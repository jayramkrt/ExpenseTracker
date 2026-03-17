package com.rotopay.expensetracker.api.v1.controller;

import com.rotopay.expensetracker.api.v1.response.TransactionResponseV1;
import com.rotopay.expensetracker.api.v1.request.TransactionUpdateRequestV1;
import com.rotopay.expensetracker.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * REST Controller for transaction operations.
 * Handles transaction retrieval, filtering, and category reclassification.
 */
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionControllerV1 {

    private final TransactionService transactionService;

    /**
     * Get all transactions for the authenticated user with advanced filtering.
     */
    @GetMapping
    public ResponseEntity<Page<TransactionResponseV1>> getTransactions(
            @RequestHeader("Authorization") String userId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String merchantName,
            @RequestParam(required = false) Boolean isManual,
            Pageable pageable) {

        log.debug("Fetching transactions for user: {} with filters", userId);

        Page<TransactionResponseV1> transactions = transactionService.getTransactions(
                UUID.fromString(userId), categoryId, startDate, endDate, merchantName, isManual, pageable);

        return ResponseEntity.ok(transactions);
    }

    /**
     * Get a specific transaction by ID.
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponseV1> getTransaction(
            @PathVariable UUID transactionId,
            @RequestHeader("Authorization") String userId) {

        log.debug("Fetching transaction: {} for user: {}", transactionId, userId);
        TransactionResponseV1 transaction = transactionService.getTransaction(transactionId, UUID.fromString(userId));

        return ResponseEntity.ok(transaction);
    }

    /**
     * Update transaction category or notes (for manual corrections).
     * Useful when LLM classification is incorrect.
     */
    @PutMapping("/{transactionId}")
    public ResponseEntity<TransactionResponseV1> updateTransaction(
            @PathVariable UUID transactionId,
            @RequestBody TransactionUpdateRequestV1 request,
            @RequestHeader("Authorization") String userId) {

        log.info("Updating transaction: {} for user: {}", transactionId, userId);
        TransactionResponseV1 updated = transactionService.updateTransaction(
                transactionId, UUID.fromString(userId), request);

        return ResponseEntity.ok(updated);
    }

    /**
     * Get transactions by category with date range.
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<TransactionResponseV1>> getTransactionsByCategory(
            @PathVariable UUID categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestHeader("Authorization") String userId) {

        log.debug("Fetching transactions by category: {} for user: {}", categoryId, userId);
        List<TransactionResponseV1> transactions = transactionService.getTransactionsByCategory(
                UUID.fromString(userId), categoryId, startDate, endDate);

        return ResponseEntity.ok(transactions);
    }

    /**
     * Search transactions by merchant name.
     */
    @GetMapping("/search")
    public ResponseEntity<Page<TransactionResponseV1>> searchTransactions(
            @RequestParam String query,
            @RequestHeader("Authorization") String userId,
            Pageable pageable) {

        log.debug("Searching transactions with query: {} for user: {}", query, userId);
        Page<TransactionResponseV1> results = transactionService.searchTransactions(
                UUID.fromString(userId), query, pageable);

        return ResponseEntity.ok(results);
    }

    /**
     * Delete a transaction.
     */
    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> deleteTransaction(
            @PathVariable UUID transactionId,
            @RequestHeader("Authorization") String userId) {

        log.info("Deleting transaction: {} for user: {}", transactionId, userId);
        transactionService.deleteTransaction(transactionId, UUID.fromString(userId));

        return ResponseEntity.noContent().build();
    }
}