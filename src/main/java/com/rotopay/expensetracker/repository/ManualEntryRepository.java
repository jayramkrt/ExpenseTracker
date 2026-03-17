package com.rotopay.expensetracker.repository;


import com.rotopay.expensetracker.entity.ManualEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repository for ManualEntry entity.
 * Handles user-manually-entered transactions.
 */
@Repository
public interface ManualEntryRepository extends JpaRepository<ManualEntry, UUID> {

    /**
     * Find all manual entries for a user with pagination.
     */
    Page<ManualEntry> findByUserIdOrderByTransactionDateDesc(UUID userId, Pageable pageable);

    /**
     * Find manual entries by user and date range.
     */
    @Query("SELECT m FROM ManualEntry m " +
            "WHERE m.userId = :userId " +
            "AND m.transactionDate BETWEEN :startDate AND :endDate " +
            "ORDER BY m.transactionDate DESC")
    Page<ManualEntry> findByUserAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    /**
     * Find manual entries by user and category.
     */
    @Query("SELECT m FROM ManualEntry m " +
            "WHERE m.userId = :userId " +
            "AND m.categoryId = :categoryId " +
            "ORDER BY m.transactionDate DESC")
    List<ManualEntry> findByUserAndCategory(
            @Param("userId") UUID userId,
            @Param("categoryId") UUID categoryId);

    /**
     * Find manual entries by user, category, and date range.
     */
    @Query("SELECT m FROM ManualEntry m " +
            "WHERE m.userId = :userId " +
            "AND m.categoryId = :categoryId " +
            "AND m.transactionDate BETWEEN :startDate AND :endDate " +
            "ORDER BY m.transactionDate DESC")
    List<ManualEntry> findByUserCategoryAndDateRange(
            @Param("userId") UUID userId,
            @Param("categoryId") UUID categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get total income from manual entries.
     */
    @Query("SELECT COALESCE(SUM(m.amount), 0) FROM ManualEntry m " +
            "WHERE m.userId = :userId " +
            "AND m.amount > 0 " +
            "AND m.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalIncomeManual(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get total expenses from manual entries.
     */
    @Query("SELECT COALESCE(SUM(m.amount), 0) FROM ManualEntry m " +
            "WHERE m.userId = :userId " +
            "AND m.amount < 0 " +
            "AND m.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalExpensesManual(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Count manual entries by user and month.
     */
    @Query("SELECT COUNT(m) FROM ManualEntry m " +
            "WHERE m.userId = :userId " +
            "AND EXTRACT(YEAR FROM m.transactionDate) = :year " +
            "AND EXTRACT(MONTH FROM m.transactionDate) = :month")
    Long countByUserAndYearMonth(
            @Param("userId") UUID userId,
            @Param("year") Integer year,
            @Param("month") Integer month);
}
