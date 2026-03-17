package com.rotopay.expensetracker.api.v1.controller;

import com.rotopay.expensetracker.api.v1.request.ManualEntryRequestV1;
import com.rotopay.expensetracker.api.v1.response.ManualEntryResponseV1;
import com.rotopay.expensetracker.service.ManualEntryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * REST Controller for manual transaction entry operations.
 * Allows users to add transactions not captured in bank statements.
 */
@RestController
@RequestMapping("/api/v1/manual-entries")
@RequiredArgsConstructor
@Slf4j
public class ManualEntryControllerV1 {

    private final ManualEntryService manualEntryService;

    /**
     * Create a new manual entry.
     */
    @PostMapping
    public ResponseEntity<ManualEntryResponseV1> createManualEntry(
            @Valid @RequestBody ManualEntryRequestV1 request,
            @RequestHeader("Authorization") String userId) {

        log.info("Creating manual entry for user: {}", userId);
        ManualEntryResponseV1 response = manualEntryService.createManualEntry(
                UUID.fromString(userId), request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all manual entries for the authenticated user.
     */
    @GetMapping
    public ResponseEntity<Page<ManualEntryResponseV1>> getManualEntries(
            @RequestHeader("Authorization") String userId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable) {

        log.debug("Fetching manual entries for user: {}", userId);
        Page<ManualEntryResponseV1> entries = manualEntryService.getManualEntries(
                UUID.fromString(userId), categoryId, startDate, endDate, pageable);

        return ResponseEntity.ok(entries);
    }

    /**
     * Get a specific manual entry by ID.
     */
    @GetMapping("/{entryId}")
    public ResponseEntity<ManualEntryResponseV1> getManualEntry(
            @PathVariable UUID entryId,
            @RequestHeader("Authorization") String userId) {

        log.debug("Fetching manual entry: {} for user: {}", entryId, userId);
        ManualEntryResponseV1 entry = manualEntryService.getManualEntry(entryId, UUID.fromString(userId));

        return ResponseEntity.ok(entry);
    }

    /**
     * Update a manual entry.
     */
    @PutMapping("/{entryId}")
    public ResponseEntity<ManualEntryResponseV1> updateManualEntry(
            @PathVariable UUID entryId,
            @Valid @RequestBody ManualEntryRequestV1 request,
            @RequestHeader("Authorization") String userId) {

        log.info("Updating manual entry: {} for user: {}", entryId, userId);
        ManualEntryResponseV1 updated = manualEntryService.updateManualEntry(
                entryId, UUID.fromString(userId), request);

        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a manual entry.
     */
    @DeleteMapping("/{entryId}")
    public ResponseEntity<Void> deleteManualEntry(
            @PathVariable UUID entryId,
            @RequestHeader("Authorization") String userId) {

        log.info("Deleting manual entry: {} for user: {}", entryId, userId);
        manualEntryService.deleteManualEntry(entryId, UUID.fromString(userId));

        return ResponseEntity.noContent().build();
    }
}
