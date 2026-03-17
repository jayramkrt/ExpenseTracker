package com.rotopay.expensetracker.service;

import com.rotopay.expensetracker.api.v1.mapper.TransactionMapper;
import com.rotopay.expensetracker.api.v1.response.TransactionResponseV1;
import com.rotopay.expensetracker.api.v1.request.TransactionUpdateRequestV1;
import com.rotopay.expensetracker.entity.Transaction;
import com.rotopay.expensetracker.api.common.exception.ResourceNotFoundException;
import com.rotopay.expensetracker.api.common.exception.UnauthorizedException;
import com.rotopay.expensetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final CategoryService categoryService;

    /**
     * Get transactions with advanced filtering.
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponseV1> getTransactions(
            UUID userId,
            UUID categoryId,
            LocalDate startDate,
            LocalDate endDate,
            String merchantName,
            Boolean isManual,
            Pageable pageable) {

        log.debug("Fetching transactions for user: {} with filters", userId);

        Page<Transaction> transactions;

        // Apply filters based on provided parameters
        if (startDate != null && endDate != null) {
            transactions = transactionRepository.findByUserAndDateRange(userId, startDate, endDate, pageable);
        } else {
            transactions = transactionRepository.findByUserIdOrderByTransactionDateDesc(userId, pageable);
        }

        // Note: For production, use Specification or QueryDSL for complex dynamic queries
        return transactions.map(transactionMapper::toResponse);
    }

    /**
     * Get a specific transaction.
     */
    @Transactional(readOnly = true)
    public TransactionResponseV1 getTransaction(UUID transactionId, UUID userId) {
        log.debug("Fetching transaction: {} for user: {}", transactionId, userId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));

        // Verify ownership
        if (!transaction.getUserId().equals(userId)) {
            throw new UnauthorizedException("User not authorized to access this transaction");
        }

        return transactionMapper.toResponse(transaction);
    }

    /**
     * Update transaction (reclassify category or add notes).
     */
    @Transactional
    public TransactionResponseV1 updateTransaction(
            UUID transactionId,
            UUID userId,
            TransactionUpdateRequestV1 request) {

        log.info("Updating transaction: {} for user: {}", transactionId, userId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));

        if (!transaction.getUserId().equals(userId)) {
            throw new UnauthorizedException("User not authorized to update this transaction");
        }

        // Update category if provided
        if (request.getCategoryId() != null) {
            // Validate category exists
            categoryService.getCategory(request.getCategoryId());
            transaction.setCategoryId(request.getCategoryId());
            transaction.setCategoryName(request.getCategoryName());
        }

        // Update notes if provided
        if (request.getNotes() != null) {
            transaction.setNotes(request.getNotes());
        }

        Transaction updated = transactionRepository.save(transaction);
        log.info("Transaction {} updated successfully", transactionId);

        return transactionMapper.toResponse(updated);
    }

    /**
     * Get transactions by category.
     */
    @Transactional(readOnly = true)
    public List<TransactionResponseV1> getTransactionsByCategory(
            UUID userId,
            UUID categoryId,
            LocalDate startDate,
            LocalDate endDate) {

        log.debug("Fetching transactions by category: {} for user: {}", categoryId, userId);

        List<Transaction> transactions;

        if (startDate != null && endDate != null) {
            transactions = transactionRepository.findByUserCategoryAndDateRange(
                    userId, categoryId, startDate, endDate);
        } else {
            transactions = transactionRepository.findByUserAndCategory(userId, categoryId);
        }

        return transactions.stream()
                .map(transactionMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Search transactions by merchant or description.
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponseV1> searchTransactions(
            UUID userId,
            String query,
            Pageable pageable) {

        log.debug("Searching transactions with query: {} for user: {}", query, userId);

        Page<Transaction> results = transactionRepository.searchByMerchantOrDescription(
                userId, query, pageable);

        return results.map(transactionMapper::toResponse);
    }

    /**
     * Delete a transaction.
     */
    @Transactional
    public void deleteTransaction(UUID transactionId, UUID userId) {
        log.info("Deleting transaction: {} for user: {}", transactionId, userId);

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));

        if (!transaction.getUserId().equals(userId)) {
            throw new UnauthorizedException("User not authorized to delete this transaction");
        }

        transactionRepository.delete(transaction);
        log.info("Transaction {} deleted successfully", transactionId);
    }


}
