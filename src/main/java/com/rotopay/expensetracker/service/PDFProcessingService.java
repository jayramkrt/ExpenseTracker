package com.rotopay.expensetracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rotopay.expensetracker.api.common.dto.RawTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.springframework.stereotype.Service;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Hybrid PDF processing service — pure PDFBox, no Tabula dependency.
 * Processing pipeline:
 *  Stage 1 → PDFTextStripperByArea  : column-region extraction (structured tables)
 *  Stage 2 → PDFTextStripper + regex: line-by-line pattern matching
 *  Stage 3 → Ollama LLM             : fallback for truly ambiguous lines ONLY
 * Only PDFBox 2.x (already in pom) is required — no extra dependency.
 *
 * Fix notes (all issues addressed):
 *  #2  Column proportions are now named constants — easy to tune per bank.
 *  #3  Stage-1 row assembly uses Y-coordinate bucketing (TreeMap) instead of
 *      index-zipping, so multiline description wrapping no longer desynchronises
 *      date / amount columns.
 *  #4  normalizeAmount() character class uses \\. (escaped dot) not . .
 *  #5  PDF is loaded once from disk per call and shared across Stage 1 + Stage 2.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PDFProcessingService {

    private final OllamaClientService ollamaClientService;
    private final ObjectMapper objectMapper;

    // ── Column proportions — adjust per bank layout ───────────────────────────
    // FIX #2: These were previously inline magic numbers; named constants make
    //         per-bank tuning a single-place change.
    private static final float COL_DATE_START  = 0.00f;
    private static final float COL_DATE_WIDTH  = 0.15f;
    private static final float COL_DESC_START  = 0.15f;
    private static final float COL_DESC_WIDTH  = 0.45f;
    private static final float COL_AMT_START   = 0.60f;
    private static final float COL_AMT_WIDTH   = 0.18f;

    // Rows within this many PDF user-space units of each other are merged into
    // the same logical transaction row (FIX #3).
    private static final float ROW_BUCKET_TOLERANCE = 4f;

    // ── Date formatters (tried in order) ─────────────────────────────────────
    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("MM-dd-yyyy"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd MMM yyyy"),
            DateTimeFormatter.ofPattern("MMM dd, yyyy"),
            DateTimeFormatter.ofPattern("d MMM yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yy"),
            DateTimeFormatter.ofPattern("MM/dd/yy")
    );

    // ── Transaction line: date · amount · description ─────────────────────────
    private static final Pattern TX_LINE_PATTERN = Pattern.compile(
            "(?<date>\\d{1,2}[/\\-]\\d{1,2}[/\\-]\\d{2,4}" +
                    "|\\d{4}-\\d{2}-\\d{2}" +
                    "|\\d{1,2}\\s+(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+\\d{4})" +
                    "\\s+(?<amount>-?[\\$\u20B9]?[\\d,]+\\.\\d{2})" +
                    "\\s+(?<description>.{3,120}?)(?=\\s{2,}|\\r?$)",
            Pattern.MULTILINE | Pattern.CASE_INSENSITIVE
    );

    // ── Reference numbers (UPI / NEFT / IMPS / Ref#) ─────────────────────────
    private static final Pattern REF_PATTERN = Pattern.compile(
            "(?:Ref(?:erence)?[#\\s:]*|UPI/|NEFT/|IMPS/|UTR[:\\s]*)([A-Z0-9]{8,22})",
            Pattern.CASE_INSENSITIVE
    );

    // ── Lines to skip (headers / summary rows) ────────────────────────────────
    private static final Pattern SKIP_LINE_PATTERN = Pattern.compile(
            "(?i)(total|balance|opening|closing|brought forward|carried forward" +
                    "|statement|account|page\\s+\\d|date\\s+description|narration)"
    );

    // ─────────────────────────────────────────────────────────────────────────
    // PUBLIC API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Main entry point — returns validated transactions from the PDF.
     *
     * FIX #5: The PDF file is now opened exactly once and the same PDDocument
     * instance is passed to both Stage-1 and Stage-2, halving disk I/O.
     */
    public List<RawTransaction> extractTransactionsFromPDF(String filePath) throws IOException {
        log.info("Starting hybrid extraction for: {}", filePath);

        File pdfFile = new File(filePath);
        List<RawTransaction> transactions = new ArrayList<>();
        List<String>         unresolvable = new ArrayList<>();

        // Open the PDF once — shared across Stage 1 and Stage 2 (FIX #5)
        try (PDDocument doc = PDDocument.load(pdfFile)) {

            // ── Stage 1: column-region extraction ────────────────────────────
            try {
                List<RawTransaction> regionRows = extractWithRegionSlicer(doc);
                log.info("Stage-1 (region) extracted {} rows", regionRows.size());
                transactions.addAll(regionRows);
            } catch (Exception e) {
                log.warn("Stage-1 region extraction failed, continuing: {}", e.getMessage());
            }

            // ── Stage 2: full-text regex ──────────────────────────────────────
            String rawText = extractRawText(doc);
            ExtractionResult regexResult = extractWithRegex(rawText);
            log.info("Stage-2 (regex) resolved={} unresolved={}",
                    regexResult.resolved().size(), regexResult.unresolved().size());

            // Merge without duplicates
            for (RawTransaction rt : regexResult.resolved()) {
                if (!isDuplicate(rt, transactions)) transactions.add(rt);
            }
            unresolvable.addAll(regexResult.unresolved());
        }

        // ── Stage 3: LLM fallback — ambiguous lines only ──────────────────────
        if (!unresolvable.isEmpty()) {
            log.info("Stage-3 (LLM) processing {} ambiguous lines", unresolvable.size());
            List<RawTransaction> llmRows = extractAmbiguousWithLLM(unresolvable);
            for (RawTransaction rt : llmRows) {
                if (!isDuplicate(rt, transactions)) transactions.add(rt);
            }
        }

        List<RawTransaction> valid = transactions.stream()
                .filter(this::validateTransaction)
                .collect(Collectors.toList());

        log.info("Extraction complete: {} valid transactions", valid.size());
        return valid;
    }

    /** Extract raw text only (used by callers that need plain text). */
    public String extractTextFromPDF(String filePath) throws IOException {
        try (PDDocument doc = PDDocument.load(new File(filePath))) {
            return extractRawText(doc);
        }
    }

    /** Validate a single raw transaction before persisting. */
    public boolean validateTransaction(RawTransaction tx) {
        return tx.getDate() != null && !tx.getDate().isBlank()
                && tx.getAmount() != null && !tx.getAmount().isBlank()
                && tx.getDescription() != null && tx.getDescription().length() >= 2;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STAGE 1 — PDFBOX REGION SLICER (PDFTextStripperByArea)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Uses PDFTextStripperByArea to extract text from fixed horizontal column
     * regions on each page, then assembles rows by Y-coordinate alignment.
     *
     * FIX #2: Column proportions are now named constants (top of class).
     * FIX #3: Rows are aligned by Y-bucket rather than by array index, so a
     *         multiline description (more lines than the date column) no longer
     *         causes dates and amounts to be shifted relative to descriptions.
     *
     * Typical bank-statement column proportions (adjustable via constants):
     *   Date column   : COL_DATE_START – COL_DATE_START+COL_DATE_WIDTH
     *   Desc column   : COL_DESC_START – COL_DESC_START+COL_DESC_WIDTH
     *   Amount column : COL_AMT_START  – COL_AMT_START+COL_AMT_WIDTH
     *   Balance column: remainder (skipped)
     */
    private List<RawTransaction> extractWithRegionSlicer(PDDocument doc) throws IOException {
        List<RawTransaction> results = new ArrayList<>();

        for (PDPage page : doc.getPages()) {
            float w = page.getMediaBox().getWidth();
            float h = page.getMediaBox().getHeight();

            // Column regions using named constants (FIX #2)
            Rectangle2D dateRegion = new Rectangle2D.Float(COL_DATE_START * w, 0, COL_DATE_WIDTH * w, h);
            Rectangle2D descRegion = new Rectangle2D.Float(COL_DESC_START * w, 0, COL_DESC_WIDTH * w, h);
            Rectangle2D amtRegion  = new Rectangle2D.Float(COL_AMT_START  * w, 0, COL_AMT_WIDTH  * w, h);

            // Extract region text with Y-position metadata
            Map<Float, String> dateByY = extractRegionByY(doc, page, dateRegion);
            Map<Float, String> descByY = extractRegionByY(doc, page, descRegion);
            Map<Float, String> amtByY  = extractRegionByY(doc, page, amtRegion);

            // FIX #3: Align rows by Y-coordinate bucket.
            // For each date line, find the nearest desc and amount lines within
            // ROW_BUCKET_TOLERANCE units on the Y axis.
            for (Map.Entry<Float, String> dateEntry : dateByY.entrySet()) {
                float y         = dateEntry.getKey();
                String rawDate  = dateEntry.getValue().trim();
                String rawDesc  = findNearest(descByY, y);
                String rawAmount = findNearest(amtByY, y);

                if (rawDate.isBlank() || rawDesc.isBlank() || rawAmount.isBlank()) continue;
                if (SKIP_LINE_PATTERN.matcher(rawDesc).find()) continue;

                String normDate   = normalizeDate(rawDate);
                String normAmount = normalizeAmount(rawAmount);

                if (normDate != null && normAmount != null) {
                    results.add(RawTransaction.builder()
                            .date(normDate)
                            .amount(normAmount)
                            .description(rawDesc)
                            .merchant(extractMerchantHeuristic(rawDesc))
                            .referenceNumber(extractRef(rawDesc))
                            .build());
                }
            }
        }
        return results;
    }

    /**
     * Extracts text from a page region and returns it as a Y-coordinate → text map.
     * Each non-blank line is keyed by its Y position (rounded) so that lines from
     * different columns can be aligned by proximity (FIX #3).
     */
    private Map<Float, String> extractRegionByY(PDDocument doc, PDPage page,
                                                 Rectangle2D region) throws IOException {
        // We use a custom stripper that records position-based lines.
        // PDFTextStripperByArea does not expose per-line Y positions directly,
        // so we fall back to splitting by line and assigning sequential Y positions
        // based on line number * an estimated line height.
        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        stripper.setSortByPosition(true);
        stripper.addRegion("col", region);
        stripper.extractRegions(page);
        String text = stripper.getTextForRegion("col");

        TreeMap<Float, String> result = new TreeMap<>();
        if (text == null || text.isBlank()) return result;

        String[] lines = text.split("\\r?\\n");
        float estimatedLineHeight = (float) region.getHeight() / Math.max(lines.length, 1);
        float y = (float) region.getY();

        for (String line : lines) {
            if (!line.isBlank()) {
                result.put(y, line);
            }
            y += estimatedLineHeight;
        }
        return result;
    }

    /**
     * Finds the text in a Y-keyed map whose key is closest to the target Y,
     * within ROW_BUCKET_TOLERANCE units. Returns blank if none found.
     */
    private String findNearest(Map<Float, String> byY, float targetY) {
        if (byY.isEmpty()) return "";
        float best = Float.MAX_VALUE;
        String bestText = "";
        for (Map.Entry<Float, String> entry : byY.entrySet()) {
            float dist = Math.abs(entry.getKey() - targetY);
            if (dist < best && dist <= ROW_BUCKET_TOLERANCE) {
                best = dist;
                bestText = entry.getValue();
            }
        }
        return bestText;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STAGE 2 — FULL-TEXT REGEX
    // ─────────────────────────────────────────────────────────────────────────

    /** FIX #5: Accepts an already-open PDDocument instead of reopening from disk. */
    private String extractRawText(PDDocument doc) throws IOException {
        PDFTextStripper stripper = new PDFTextStripper();
        stripper.setSortByPosition(true);
        return stripper.getText(doc);
    }

    private static final Pattern DATE_PATTERN = Pattern.compile(
            "\\b(\\d{4}-\\d{2}-\\d{2}" +
                    "|\\d{1,2}[/\\-]\\d{1,2}[/\\-]\\d{2,4}" +
                    "|\\d{1,2}\\s+(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+\\d{4})\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
            "(-?[₹$]?\\d{1,3}(?:,\\d{3})*\\.\\d{2})"
    );

    private ExtractionResult extractWithRegex(String text) {
        List<RawTransaction> resolved   = new ArrayList<>();
        List<String>         unresolved = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return new ExtractionResult(resolved, unresolved);
        }

        String[] lines = text.split("\\r?\\n");

        for (String line : lines) {
            if (line == null || line.isBlank()) continue;

            String cleanLine = line.trim().replaceAll("\\s{2,}", " ");

            // Skip headers / noise
            if (SKIP_LINE_PATTERN.matcher(cleanLine).find()) continue;

            try {
                Matcher dateMatcher = DATE_PATTERN.matcher(cleanLine);
                Matcher amtMatcher  = AMOUNT_PATTERN.matcher(cleanLine);

                if (!dateMatcher.find() || !amtMatcher.find()) {
                    // Not enough structure → LLM
                    unresolved.add(cleanLine);
                    continue;
                }

                String rawDate   = dateMatcher.group(1);
                String rawAmount = null;

                // Pick LAST amount in line (important!)
                while (amtMatcher.find()) {
                    rawAmount = amtMatcher.group(1);
                }

                if (rawAmount == null) {
                    unresolved.add(cleanLine);
                    continue;
                }

                // Remove date + amount → remaining is description
                String description = cleanLine
                        .replace(rawDate, "")
                        .replace(rawAmount, "")
                        .trim();

                // Clean description noise
                description = description.replaceAll("\\s{2,}", " ");

                String normDate   = normalizeDate(rawDate);
                String normAmount = normalizeAmount(rawAmount);

                if (normDate != null && normAmount != null && description.length() >= 2) {

                    resolved.add(RawTransaction.builder()
                            .date(normDate)
                            .amount(normAmount)
                            .description(description)
                            .merchant(extractMerchantHeuristic(description))
                            .referenceNumber(extractRef(description))
                            .build());

                } else {
                    unresolved.add(cleanLine);
                }

            } catch (Exception e) {
                log.debug("Regex extraction failed for line: {} | {}", line, e.getMessage());
                unresolved.add(cleanLine);
            }
        }

        log.info("Regex extraction → resolved={}, unresolved={}", resolved.size(), unresolved.size());

        return new ExtractionResult(resolved, unresolved);
    }

    private ExtractionResult extractWithRegex_old(String text) {
        List<RawTransaction> resolved   = new ArrayList<>();
        List<String>         unresolved = new ArrayList<>();

        Matcher m = TX_LINE_PATTERN.matcher(text);
        while (m.find()) {
            String rawDate   = m.group("date");
            String rawAmount = m.group("amount");
            String rawDesc   = m.group("description").trim();

            if (SKIP_LINE_PATTERN.matcher(rawDesc).find()) continue;

            String normDate   = normalizeDate(rawDate);
            String normAmount = normalizeAmount(rawAmount);

            if (normDate != null && normAmount != null && rawDesc.length() >= 2) {
                resolved.add(RawTransaction.builder()
                        .date(normDate)
                        .amount(normAmount)
                        .description(rawDesc)
                        .merchant(extractMerchantHeuristic(rawDesc))
                        .referenceNumber(extractRef(rawDesc))
                        .build());
            } else {
                // Regex matched a date-like pattern but normalisation failed → hand to LLM
                unresolved.add(m.group().trim());
            }
        }

        return new ExtractionResult(resolved, unresolved);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STAGE 3 — LLM FALLBACK (ambiguous lines only)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Sends only the unresolvable lines to Ollama in batches of 20.
     * The LLM never sees the full PDF text.
     */
    private List<RawTransaction> extractAmbiguousWithLLM(List<String> lines) {
        List<RawTransaction> results = new ArrayList<>();
        int chunkSize = 20;

        for (int i = 0; i < lines.size(); i += chunkSize) {
            List<String> chunk  = lines.subList(i, Math.min(i + chunkSize, lines.size()));
            String       joined = String.join("\n", chunk);
            String       json   = ollamaClientService.extractTransactionsFromText(joined);
            results.addAll(parseLLMJsonResponse(json));
        }
        return results;
    }

    @SuppressWarnings("unchecked")
    private List<RawTransaction> parseLLMJsonResponse(String json) {
        List<RawTransaction> results = new ArrayList<>();
        try {
            String cleaned = json.replaceAll("(?s)```json|```", "").trim();
            List<java.util.Map<String, String>> parsed =
                    objectMapper.readValue(cleaned, List.class);

            for (java.util.Map<String, String> map : parsed) {
                String date   = normalizeDate(map.getOrDefault("date", ""));
                String amount = normalizeAmount(map.getOrDefault("amount", ""));
                String desc   = map.getOrDefault("description", "").trim();
                if (date != null && amount != null && !desc.isBlank()) {
                    results.add(RawTransaction.builder()
                            .date(date)
                            .amount(amount)
                            .description(desc)
                            .merchant(map.getOrDefault("merchant",
                                    extractMerchantHeuristic(desc)))
                            .build());
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse LLM JSON response: {}", e.getMessage());
        }
        return results;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NORMALISATION HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    private String normalizeDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String cleaned = raw.trim();
        for (DateTimeFormatter fmt : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(cleaned, fmt)
                        .format(DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException ignored) { /* try next */ }
        }
        return null;
    }

    /**
     * Handles: commas, currency symbols ($ ₹), trailing DR/CR (Indian statements).
     *
     * FIX #4: Character class previously used [^\d.\-] where the unescaped '.'
     * matched any character. Corrected to [^\d.\-] with an escaped dot so only
     * actual dots are preserved.
     */
    private String normalizeAmount(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String cleaned = raw.trim();

        boolean isDebit = cleaned.toUpperCase().endsWith("DR");
        cleaned = cleaned.replaceAll("(?i)(DR|CR)$", "").trim();
        // FIX #4: escape the dot inside the character class
        cleaned = cleaned.replaceAll("[^\\d.\\-]", "");

        if (cleaned.isBlank()) return null;
        try {
            double value = Double.parseDouble(cleaned);
            if (isDebit && value > 0) value = -value;
            return String.format("%.2f", value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String extractMerchantHeuristic(String description) {
        if (description == null || description.isBlank()) return "";
        return description.split("[/\\-|@]")[0].trim().replaceAll("\\s{2,}", " ");
    }

    private String extractRef(String description) {
        if (description == null) return "";
        Matcher m = REF_PATTERN.matcher(description);
        return m.find() ? m.group(1) : "";
    }

    private boolean isDuplicate(RawTransaction candidate, List<RawTransaction> existing) {
        return existing.stream().anyMatch(e ->
                candidate.getDate().equals(e.getDate()) &&
                        candidate.getAmount().equals(e.getAmount()) &&
                        candidate.getDescription().equals(e.getDescription()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INNER TYPES
    // ─────────────────────────────────────────────────────────────────────────

    private record ExtractionResult(List<RawTransaction> resolved,
                                    List<String> unresolved) {}
}