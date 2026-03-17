package com.rotopay.expensetracker.api.v1.controller;

import com.rotopay.expensetracker.api.v1.response.StatementResponseV1;
import com.rotopay.expensetracker.api.v1.request.StatementUploadRequestV1;
import com.rotopay.expensetracker.service.BankStatementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for bank statement operations.
 * Handles PDF uploads, statement retrieval, and processing status tracking.
 */
@RestController
@RequestMapping(path = "/api/v1/statements")
@RequiredArgsConstructor
@Slf4j
public class BankStatementControllerV1 {

    final BankStatementService statementService;

    /**
     * Upload a bank statement PDF file.
     * Initiates async processing: PDF extraction → LLM classification
     */
    @PostMapping("/upload")
    public ResponseEntity<StatementResponseV1> uploadStatement(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String bankName,
            @RequestParam(required = false) String accountType,
            @RequestHeader("Authorization") String userId
            ){
        log.info("Uploading statement: {} for user: {}", file.getOriginalFilename(), userId);
        StatementUploadRequestV1 request = StatementUploadRequestV1.builder().
                file(file)
                .bankName(bankName)
                .accountType(accountType)
                .userId(UUID.fromString(userId))
                .build();

        StatementResponseV1 response = statementService.uploadStatement(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all statements for the authenticated user with pagination.
     */
    @GetMapping
    public ResponseEntity<Page<StatementResponseV1>> getUserStatements(
            @RequestHeader("Authorization") String userId,
            Pageable pageable) {

        log.debug("Fetching statements for user: {}", userId);
        Page<StatementResponseV1> statements = statementService.getUserStatements(
                UUID.fromString(userId), pageable);

        return ResponseEntity.ok(statements);
    }

    /**
     * Get a specific statement by ID.
     */
    @GetMapping("/{statementId}")
    public ResponseEntity<StatementResponseV1> getStatement(
            @PathVariable UUID statementId,
            @RequestHeader("Authorization") String userId) {

        log.debug("Fetching statement: {} for user: {}", statementId, userId);
        StatementResponseV1 statement = statementService.getStatement(statementId, UUID.fromString(userId));

        return ResponseEntity.ok(statement);
    }

    /**
     * Get processing status of a statement.
     * Used for polling progress during async PDF extraction and classification.
     */
    @GetMapping("/{statementId}/status")
    public ResponseEntity<Map<String, Object>> getStatementStatus(
            @PathVariable UUID statementId,
            @RequestHeader("Authorization") String userId) {

        StatementResponseV1 statement = statementService.getStatement(statementId, UUID.fromString(userId));

        return ResponseEntity.ok(Map.of(
                "statementId", statement.getId(),
                "status", statement.getProcessingStatus(),
                "transactionCount", statement.getTransactionCount(),
                "errorMessage", statement.getErrorMessage()
        ));
    }

    /**
     * Delete a statement and all its associated transactions.
     */
    @DeleteMapping("/{statementId}")
    public ResponseEntity<Void> deleteStatement(
            @PathVariable UUID statementId,
            @RequestHeader("Authorization") String userId) {

        log.info("Deleting statement: {} for user: {}", statementId, userId);
        statementService.deleteStatement(statementId, UUID.fromString(userId));

        return ResponseEntity.noContent().build();
    }

    /**
     * Retry processing a failed statement.
     */
    @PostMapping("/{statementId}/retry")
    public ResponseEntity<StatementResponseV1> retryProcessing(
            @PathVariable UUID statementId,
            @RequestHeader("Authorization") String userId) {

        log.info("Retrying statement processing: {}", statementId);
        StatementResponseV1 response = statementService.retryProcessing(statementId, UUID.fromString(userId));

        return ResponseEntity.ok(response);
    }
}
