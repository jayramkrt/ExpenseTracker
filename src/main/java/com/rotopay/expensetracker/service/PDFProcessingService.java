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
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PDFProcessingService {

    private final OllamaClientService ollamaClientService;
    private final ObjectMapper objectMapper;

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
     */
    public List<RawTransaction> extractTransactionsFromPDF(String filePath) throws IOException {
        log.info("Starting hybrid extraction for: {}", filePath);

        File pdfFile = new File(filePath);
        List<RawTransaction> transactions  = new ArrayList<>();
        List<String>         unresolvable  = new ArrayList<>();

        // ── Stage 1: column-region extraction ────────────────────────────────
        try {
            List<RawTransaction> regionRows = extractWithRegionSlicer(pdfFile);
            log.info("Stage-1 (region) extracted {} rows", regionRows.size());
            transactions.addAll(regionRows);
        } catch (Exception e) {
            log.warn("Stage-1 region extraction failed, continuing: {}", e.getMessage());
        }

        // ── Stage 2: full-text regex ──────────────────────────────────────────
        String rawText = extractRawText(pdfFile);
        ExtractionResult regexResult = extractWithRegex(rawText);
        log.info("Stage-2 (regex) resolved={} unresolved={}",
                regexResult.resolved().size(), regexResult.unresolved().size());

        // Merge without duplicates
        for (RawTransaction rt : regexResult.resolved()) {
            if (!isDuplicate(rt, transactions)) transactions.add(rt);
        }
        unresolvable.addAll(regexResult.unresolved());

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
        return extractRawText(new File(filePath));
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
     * regions on each page, then assembles rows by aligning across regions.
     *
     * Typical bank-statement column proportions (adjustable per bank):
     *   Date column   : 0 – 15% of page width
     *   Desc column   : 15 – 60% of page width
     *   Amount column : 60 – 78% of page width
     *   Balance column: 78 – 100% (skipped)
     */
    private List<RawTransaction> extractWithRegionSlicer(File pdfFile) throws IOException {
        List<RawTransaction> results = new ArrayList<>();

        try (PDDocument doc = PDDocument.load(pdfFile)) {
            for (PDPage page : doc.getPages()) {
                float w = page.getMediaBox().getWidth();
                float h = page.getMediaBox().getHeight();

                Rectangle2D dateRegion = new Rectangle2D.Float(0,        0, w * 0.15f, h);
                Rectangle2D descRegion = new Rectangle2D.Float(w * 0.15f, 0, w * 0.45f, h);
                Rectangle2D amtRegion  = new Rectangle2D.Float(w * 0.60f, 0, w * 0.18f, h);

                String dateText = extractRegion(doc, page, dateRegion);
                String descText = extractRegion(doc, page, descRegion);
                String amtText  = extractRegion(doc, page, amtRegion);

                List<String> dates   = splitLines(dateText);
                List<String> descs   = splitLines(descText);
                List<String> amounts = splitLines(amtText);

                int count = Math.min(dates.size(), Math.min(descs.size(), amounts.size()));
                for (int i = 0; i < count; i++) {
                    String rawDate   = dates.get(i).trim();
                    String rawDesc   = descs.get(i).trim();
                    String rawAmount = amounts.get(i).trim();

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
        }
        return results;
    }

    private String extractRegion(PDDocument doc, PDPage page, Rectangle2D region) throws IOException {
        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        stripper.setSortByPosition(true);
        stripper.addRegion("col", region);
        stripper.extractRegions(page);
        return stripper.getTextForRegion("col");
    }

    private List<String> splitLines(String text) {
        if (text == null || text.isBlank()) return List.of();
        return Arrays.stream(text.split("\\r?\\n"))
                .filter(l -> !l.isBlank())
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // STAGE 2 — FULL-TEXT REGEX
    // ─────────────────────────────────────────────────────────────────────────

    private String extractRawText(File pdfFile) throws IOException {
        try (PDDocument doc = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(doc);
        }
    }

    private ExtractionResult extractWithRegex(String text) {
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
     */
    private String normalizeAmount(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String cleaned = raw.trim();

        boolean isDebit = cleaned.toUpperCase().endsWith("DR");
        cleaned = cleaned.replaceAll("(?i)(DR|CR)$", "").trim();
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