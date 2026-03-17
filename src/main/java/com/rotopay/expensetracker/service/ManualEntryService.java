package com.rotopay.expensetracker.service;

import com.rotopay.expensetracker.api.v1.mapper.ManualEntryMapper;
import com.rotopay.expensetracker.api.v1.request.ManualEntryRequestV1;
import com.rotopay.expensetracker.api.v1.response.ManualEntryResponseV1;
import com.rotopay.expensetracker.entity.ManualEntry;
import com.rotopay.expensetracker.api.common.exception.ResourceNotFoundException;
import com.rotopay.expensetracker.api.common.exception.UnauthorizedException;
import com.rotopay.expensetracker.repository.ManualEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Service for manual entry operations.
 * Handles user-manually-entered transactions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ManualEntryService {

    private final ManualEntryRepository manualEntryRepository;
    private final ManualEntryMapper manualEntryMapper;
    private final CategoryService categoryService;

    /**
     * Create a new manual entry.
     */
    @Transactional
    public ManualEntryResponseV1 createManualEntry(UUID userId, ManualEntryRequestV1 request) {
        log.info("Creating manual entry for user: {}", userId);

        // Validate category exists
        categoryService.getCategory(request.getCategoryId());

        ManualEntry entry = ManualEntry.builder()
                .userId(userId)
                .transactionDate(request.getTransactionDate())
                .amount(request.getAmount())
                .description(request.getDescription())
                .categoryId(request.getCategoryId())
                .transactionType(request.getTransactionType())
                .notes(request.getNotes())
                .build();

        ManualEntry saved = manualEntryRepository.save(entry);
        log.info("Manual entry {} created successfully", saved.getId());

        return manualEntryMapper.toResponse(saved);
    }

    /**
     * Get manual entries with filtering.
     */
    @Transactional(readOnly = true)
    public Page<ManualEntryResponseV1> getManualEntries(
            UUID userId,
            UUID categoryId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable) {

        log.debug("Fetching manual entries for user: {}", userId);

        Page<ManualEntry> entries;

        if (startDate != null && endDate != null) {
            entries = manualEntryRepository.findByUserAndDateRange(
                    userId, startDate, endDate, pageable);
        } else {
            entries = manualEntryRepository.findByUserIdOrderByTransactionDateDesc(userId, pageable);
        }

        return entries.map(manualEntryMapper::toResponse);
    }

    /**
     * Get a specific manual entry.
     */
    @Transactional(readOnly = true)
    public ManualEntryResponseV1 getManualEntry(UUID entryId, UUID userId) {
        log.debug("Fetching manual entry: {} for user: {}", entryId, userId);

        ManualEntry entry = manualEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Manual entry not found: " + entryId));

        if (!entry.getUserId().equals(userId)) {
            throw new UnauthorizedException("User not authorized to access this entry");
        }

        return manualEntryMapper.toResponse(entry);
    }

    /**
     * Update a manual entry.
     */
    @Transactional
    public ManualEntryResponseV1 updateManualEntry(
            UUID entryId,
            UUID userId,
            ManualEntryRequestV1 request) {

        log.info("Updating manual entry: {} for user: {}", entryId, userId);

        ManualEntry entry = manualEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Manual entry not found: " + entryId));

        if (!entry.getUserId().equals(userId)) {
            throw new UnauthorizedException("User not authorized to update this entry");
        }

        // Validate category
        categoryService.getCategory(request.getCategoryId());

        entry.setTransactionDate(request.getTransactionDate());
        entry.setAmount(request.getAmount());
        entry.setDescription(request.getDescription());
        entry.setCategoryId(request.getCategoryId());
        entry.setTransactionType(request.getTransactionType());
        entry.setNotes(request.getNotes());

        ManualEntry updated = manualEntryRepository.save(entry);
        log.info("Manual entry {} updated successfully", entryId);

        return manualEntryMapper.toResponse(updated);
    }

    /**
     * Delete a manual entry.
     */
    @Transactional
    public void deleteManualEntry(UUID entryId, UUID userId) {
        log.info("Deleting manual entry: {} for user: {}", entryId, userId);

        ManualEntry entry = manualEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Manual entry not found: " + entryId));

        if (!entry.getUserId().equals(userId)) {
            throw new UnauthorizedException("User not authorized to delete this entry");
        }

        manualEntryRepository.delete(entry);
        log.info("Manual entry {} deleted successfully", entryId);
    }
}
