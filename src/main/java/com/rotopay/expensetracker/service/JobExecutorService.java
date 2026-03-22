package com.rotopay.expensetracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rotopay.expensetracker.api.common.dto.RawTransaction;
import com.rotopay.expensetracker.api.common.dto.TransactionClassificationResult;
import com.rotopay.expensetracker.entity.BankStatement;
import com.rotopay.expensetracker.entity.ProcessingQueue;
import com.rotopay.expensetracker.entity.Transaction;
import com.rotopay.expensetracker.repository.BankStatementRepository;
import com.rotopay.expensetracker.repository.ProcessingQueueRepository;
import com.rotopay.expensetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Scheduled job executor for the ProcessingQueue.
 *
 * Pipeline per job:
 * 1. Claim job (pending → in_progress)
 * 2. Load BankStatement → get filePath
 * 3. PDFBox: extract raw text from PDF
 * 4. Regex: try pattern-based transaction parsing
 * 5. Ollama: fallback LLM extraction if regex yields nothing
 * 6. Ollama: classify each transaction (category + confidence)
 * 7. Ollama: detect recurring flag
 * 8. Save Transaction entities to DB
 * 9. Update BankStatement (status, transactionCount)
 * 10. Mark job completed (or failed with retry logic)
 *
 * Scheduling:
 * - Main poller : every 10 seconds — picks up pending jobs
 * - Retry poller: every 5 minutes — re-queues retryable failed jobs
 * - Stale guard : every 15 minutes — resets stuck in_progress jobs
 */
@Service
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class JobExecutorService {

    private final ProcessingQueueRepository processingQueueRepository;
    private final BankStatementRepository bankStatementRepository;
    private final TransactionRepository transactionRepository;
    private final PDFProcessingService pdfProcessingService;
    private final OllamaClientService ollamaClientService;
    private final TransactionRuleClassifier ruleClassifier;
    private final ObjectMapper objectMapper;

    // How many jobs to process per scheduler tick (prevents memory spikes)
    private static final int BATCH_SIZE = 5;

    // Jobs stuck in_progress longer than this are considered stale
    private static final int STALE_MINUTES = 30;

    // =========================================================================
    // SCHEDULER 1 — Main job poller (every 10 seconds)
    // =========================================================================

    @Scheduled(fixedDelay = 10_000)
    public void pollAndExecutePendingJobs() {
        List<ProcessingQueue> jobs = processingQueueRepository
                .findByStatusAndJobTypeOrderByCreatedAtAsc("pending", "extract_pdf");

        if (jobs.isEmpty())
            return;

        log.info("Job poller found {} pending job(s)", jobs.size());

        jobs.stream()
                .limit(BATCH_SIZE)
                .forEach(job -> {
                    try {
                        executeJob(job);
                    } catch (Exception e) {
                        // Each job is isolated — one failure doesn't stop others
                        log.error("Unexpected error executing job {}: {}", job.getId(), e.getMessage(), e);
                    }
                });
    }

    // =========================================================================
    // SCHEDULER 2 — Retry failed jobs (every 5 minutes)
    // =========================================================================

    @Scheduled(fixedDelay = 300_000)
    public void retryFailedJobs() {
        List<ProcessingQueue> retryable = processingQueueRepository.findRetryableFailedJobs();

        if (retryable.isEmpty())
            return;

        log.info("Retry poller found {} retryable failed job(s)", retryable.size());

        retryable.forEach(job -> {
            log.info("Re-queuing failed job {} (attempt {}/{})",
                    job.getId(), job.getRetryCount() + 1, job.getMaxRetries());
            job.setStatus("pending");
            job.setErrorMessage(null);
            processingQueueRepository.save(job);
        });
    }

    // =========================================================================
    // SCHEDULER 3 — Stale job cleanup (every 15 minutes)
    // =========================================================================

    @Scheduled(fixedDelay = 900_000)
    public void resetStaleJobs() {
        List<ProcessingQueue> stale = processingQueueRepository.findInProgressJobs()
                .stream()
                .filter(job -> job.getStartedAt() != null &&
                        job.getStartedAt().isBefore(LocalDateTime.now().minusMinutes(STALE_MINUTES)))
                .toList();

        if (stale.isEmpty())
            return;

        log.warn("Found {} stale in_progress job(s) — resetting to pending", stale.size());

        stale.forEach(job -> {
            job.setStatus("pending");
            job.setStartedAt(null);
            processingQueueRepository.save(job);
        });
    }

    // =========================================================================
    // CORE JOB EXECUTOR
    // =========================================================================

    @Transactional
    public void executeJob(ProcessingQueue job) {
        log.info("▶ Starting job {} | statement: {}", job.getId(), job.getStatementId());

        // ── STEP 1: Claim job atomically ─────────────────────────────────────
        if (!claimJob(job)) {
            log.warn("Job {} already claimed by another thread, skipping", job.getId());
            return;
        }

        // ── STEP 2: Load BankStatement ────────────────────────────────────────
        Optional<BankStatement> statementOpt = bankStatementRepository.findById(job.getStatementId());
        if (statementOpt.isEmpty()) {
            failJob(job, "BankStatement not found: " + job.getStatementId());
            return;
        }

        BankStatement statement = statementOpt.get();
        statement.setProcessingStatus("processing");
        bankStatementRepository.save(statement);

        try {
            // ── STEP 3 + 4 + 5: Extract transactions from PDF ────────────────
            log.info("Extracting transactions from: {}", statement.getFilePath());
            List<RawTransaction> rawTransactions = pdfProcessingService
                    .extractTransactionsFromPDF(statement.getFilePath());

            if (rawTransactions.isEmpty()) {
                log.warn("No transactions extracted from statement {}", statement.getId());
                completeJob(job, statement, 0);
                return;
            }

            log.info("Extracted {} raw transactions, starting classification...", rawTransactions.size());

            // ── STEP 6 + 7: Classify + detect recurring via Ollama ────────────
            List<Transaction> transactions = classifyAndBuildTransactions(
                    rawTransactions, statement);

            // ── STEP 8: Persist transactions ──────────────────────────────────
            List<Transaction> saved = transactionRepository.saveAll(transactions);
            log.info("Saved {} transactions for statement {}", saved.size(), statement.getId());

            // ── STEP 9 + 10: Mark everything complete ─────────────────────────
            completeJob(job, statement, saved.size());

        } catch (Exception e) {
            log.error("Job {} failed for statement {}: {}", job.getId(), job.getStatementId(), e.getMessage(), e);
            failJob(job, e.getMessage());
            statement.setProcessingStatus("failed");
            statement.setErrorMessage(truncate(e.getMessage(), 500));
            bankStatementRepository.save(statement);
        }
    }

    // =========================================================================
    // PRIVATE HELPERS
    // =========================================================================

    /**
     * Atomically claim a job via a single UPDATE WHERE status = 'pending'.
     * If another thread already claimed it the UPDATE touches 0 rows → returns false.
     * This eliminates the TOCTOU race that existed with the old read-then-write approach.
     */
    private boolean claimJob(ProcessingQueue job) {
        LocalDateTime now = LocalDateTime.now();
        int updated = processingQueueRepository.atomicClaimJob(job.getId(), now);
        if (updated == 0) {
            return false; // already claimed by another thread
        }
        // Sync in-memory fields
        job.setStatus("in_progress");
        job.setStartedAt(now);
        return true;
    }

    /**
     * Classify each RawTransaction and build Transaction entities.
     *
     * <p>Classification order (two layers to minimise LLM calls):
     * <ol>
     *   <li><b>Rule-based</b> — keyword/brand map in {@link TransactionRuleClassifier}.
     *       Covers ~70-90 % of typical Indian bank statement transactions.</li>
     *   <li><b>Ollama LLM fallback</b> — only invoked when rules return {@code null}
     *       (i.e. no keyword matched with enough confidence).</li>
     * </ol>
     *
     * <p>Same two-layer strategy applies to recurring detection.
     */
    private List<Transaction> classifyAndBuildTransactions(
            List<RawTransaction> rawTransactions,
            BankStatement statement) {

        List<Transaction> transactions = new ArrayList<>();

        // Counters for efficency reporting
        AtomicInteger categoryRuleHits  = new AtomicInteger();
        AtomicInteger categoryLlmHits   = new AtomicInteger();
        AtomicInteger recurringRuleHits = new AtomicInteger();
        AtomicInteger recurringLlmHits  = new AtomicInteger();

        for (RawTransaction raw : rawTransactions) {
            try {
                String merchant = raw.getMerchant() != null ? raw.getMerchant() : "";

                // ── LAYER 1: Rule-based category classification ────────────────
                TransactionClassificationResult classification =
                        ruleClassifier.classifyByRules(raw.getDescription(), merchant, raw.getAmount());

                if (classification != null) {
                    categoryRuleHits.incrementAndGet();
                } else {
                    // ── LAYER 2: Ollama fallback ───────────────────────────────
                    log.debug("No rule match for '{}', falling back to LLM", raw.getDescription());
                    classification = ollamaClientService.classifyTransaction(
                            raw.getDescription(), raw.getAmount());
                    categoryLlmHits.incrementAndGet();
                }

                // ── LAYER 1: Rule-based recurring detection ────────────────────
                Boolean recurringRule = ruleClassifier.detectRecurringByRules(raw.getDescription(), merchant);
                boolean recurring;

                if (recurringRule != null) {
                    recurring = recurringRule;
                    recurringRuleHits.incrementAndGet();
                } else {
                    // ── LAYER 2: Ollama fallback ───────────────────────────────
                    log.debug("No recurring rule for '{}', falling back to LLM", raw.getDescription());
                    recurring = ollamaClientService.detectRecurring(merchant, raw.getDescription());
                    recurringLlmHits.incrementAndGet();
                }

                // Parse amount — strip currency symbols, handle Indian comma format
                BigDecimal amount = parseAmount(raw.getAmount());

                Transaction transaction = Transaction.builder()
                        .userId(statement.getUserId())
                        .statementId(statement.getId())
                        .transactionDate(parseDate(raw.getDate()))
                        .amount(amount)
                        .rawDescription(raw.getDescription())
                        .merchantName(merchant.isBlank() ? extractMerchantFallback(raw.getDescription()) : merchant)
                        .referenceNumber(raw.getReferenceNumber())
                        .categoryName(classification.getCategory())
                        .confidenceScore(classification.getConfidenceScore())
                        .llmReasoning(classification.getReasoning())
                        .transactionType(amount.compareTo(BigDecimal.ZERO) < 0 ? "debit" : "credit")
                        .isManual(false)
                        .isRecurring(recurring)
                        .build();

                transactions.add(transaction);

            } catch (Exception e) {
                log.warn("Skipping transaction '{}' due to parse/classification error: {}",
                        raw.getDescription(), e.getMessage());
            }
        }

        int total = rawTransactions.size();
        log.info("Classification complete — {}/{} by rules, {}/{} via LLM | "
                + "Recurring — {}/{} by rules, {}/{} via LLM",
                categoryRuleHits.get(), total, categoryLlmHits.get(), total,
                recurringRuleHits.get(), total, recurringLlmHits.get(), total);

        return transactions;
    }

    private void completeJob(ProcessingQueue job, BankStatement statement, int txnCount) {
        job.setStatus("completed");
        job.setCompletedAt(LocalDateTime.now());
        processingQueueRepository.save(job);

        statement.setProcessingStatus("completed");
        statement.setTransactionCount(txnCount);
        statement.setErrorMessage(null);
        bankStatementRepository.save(statement);

        log.info("✓ Job {} completed — {} transactions saved for statement {}",
                job.getId(), txnCount, statement.getId());
    }

    private void failJob(ProcessingQueue job, String errorMessage) {
        job.setRetryCount(job.getRetryCount() + 1);
        job.setErrorMessage(truncate(errorMessage, 500));
        // job.setStatus(job.canRetry() ? "failed" : "failed"); // always failed; retry
        // poller re-queues
        job.setStatus("failed");
        job.setCompletedAt(LocalDateTime.now());
        processingQueueRepository.save(job);

        log.error("✗ Job {} failed (retry {}/{}): {}",
                job.getId(), job.getRetryCount(), job.getMaxRetries(), errorMessage);
    }

    /**
     * Parse amount string to BigDecimal.
     * Handles: "4,299.00", "-4299.00", "4299.00 Dr", "4299.00 Cr"
     */
    private BigDecimal parseAmount(String raw) {
        if (raw == null || raw.isBlank())
            return BigDecimal.ZERO;

        boolean isDebit = raw.toUpperCase().contains("DR") || raw.startsWith("-");

        // Strip everything except digits, dot, minus
        String cleaned = raw
                .replaceAll("[^\\d.-]", "")
                .replaceAll(",", "");

        if (cleaned.isBlank())
            return BigDecimal.ZERO;

        BigDecimal value = new BigDecimal(cleaned).abs();
        return isDebit ? value.negate() : value;
    }

    /**
     * Parse date string to LocalDate.
     * Tries common Indian/international formats.
     */
    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank())
            return LocalDate.now();

        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ofPattern("dd MMM yyyy"),
                DateTimeFormatter.ofPattern("d MMM yyyy"));

        for (DateTimeFormatter fmt : formatters) {
            try {
                return LocalDate.parse(raw.trim(), fmt);
            } catch (DateTimeParseException ignored) {
            }
        }

        log.warn("Could not parse date '{}', defaulting to today", raw);
        return LocalDate.now();
    }

    /** First word of description as fallback merchant name */
    private String extractMerchantFallback(String description) {
        if (description == null || description.isBlank())
            return "Unknown";
        return description.split("\\s+")[0];
    }

    private String truncate(String s, int maxLen) {
        if (s == null)
            return null;
        return s.length() <= maxLen ? s : s.substring(0, maxLen);
    }
}