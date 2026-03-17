package com.rotopay.expensetracker.repository;


import com.rotopay.expensetracker.entity.BankStatement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for BankStatement entity.
 * Handles statement retrieval, filtering, and status tracking.
 */
@Repository
public interface BankStatementRepository extends JpaRepository<BankStatement, UUID> {

    /**
     * Find all statements for a user with pagination.
     */
    Page<BankStatement> findByUserIdOrderByUploadedAtDesc(UUID userId, Pageable pageable);

    /**
     * Find all statements for a user without pagination.
     */
    List<BankStatement> findByUserIdOrderByUploadedAtDesc(UUID userId);

    /**
     * Find statements by user and processing status.
     */
    List<BankStatement> findByUserIdAndProcessingStatus(UUID userId, String processingStatus);

    /**
     * Find pending statements (for async processing queue).
     */
    @Query("SELECT bs FROM BankStatement bs WHERE bs.processingStatus = 'pending' ORDER BY bs.createdAt ASC")
    List<BankStatement> findPendingStatements();

    /**
     * Find statements within a date range.
     */
    @Query("SELECT bs FROM BankStatement bs " +
            "WHERE bs.userId = :userId " +
            "AND bs.statementPeriodStart >= :startDate " +
            "AND bs.statementPeriodEnd <= :endDate " +
            "ORDER BY bs.statementPeriodStart DESC")
    Page<BankStatement> findByUserAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    /**
     * Count statements by user and status.
     */
    Long countByUserIdAndProcessingStatus(UUID userId, String processingStatus);

    /**
     * Get the most recently uploaded statement for a user.
     */
    Optional<BankStatement> findFirstByUserIdOrderByUploadedAtDesc(UUID userId);
}