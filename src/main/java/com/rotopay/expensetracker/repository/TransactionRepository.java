package com.rotopay.expensetracker.repository;

import com.rotopay.expensetracker.entity.Transaction;
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
 * Repository for Transaction entity.
 * Core data access for transactions with advanced filtering and analytics.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
/*
    // Basic queries
    List<Transaction> findByUserId(UUID userId);

    List<Transaction> findByUserIdAndCategoryId(UUID userId, UUID categoryId);

    List<Transaction> findByStatementId(UUID statementId);

    List<Transaction> findByUserIdAndTransactionDateBetween(UUID userId, LocalDate startDate, LocalDate endDate);

    List<Transaction> findByUserIdAndIsManualTrue(UUID userId);

    // =====================================================================
    // FIXED: Monthly breakdown query (use YEAR and MONTH functions)
    // =====================================================================
    @Query("SELECT " +
            "CONCAT(CAST(YEAR(t.transactionDate) as string), '-', " +
            "       LPAD(CAST(MONTH(t.transactionDate) as string), 2, '0')) as yearMonth, " +
            "t.categoryId as categoryId, " +
            "AVG(ABS(t.amount)) as avgAmount " +
            "FROM Transaction t " +
            "WHERE t.userId = :userId AND t.amount < 0 " +
            "GROUP BY YEAR(t.transactionDate), MONTH(t.transactionDate), t.categoryId " +
            "ORDER BY yearMonth DESC, avgAmount DESC")
    List<Object[]> findMonthlySpendingByCategory(@Param("userId") UUID userId);

    // =====================================================================
    // Alternative: Simpler query without date formatting
    // =====================================================================
    @Query("SELECT " +
            "YEAR(t.transactionDate) as year, " +
            "MONTH(t.transactionDate) as month, " +
            "t.categoryId as categoryId, " +
            "SUM(ABS(t.amount)) as totalAmount, " +
            "AVG(ABS(t.amount)) as avgAmount, " +
            "COUNT(t) as transactionCount " +
            "FROM Transaction t " +
            "WHERE t.userId = :userId AND t.amount < 0 " +
            "GROUP BY YEAR(t.transactionDate), MONTH(t.transactionDate), t.categoryId " +
            "ORDER BY year DESC, month DESC, avgAmount DESC")
    List<Object[]> findMonthlySpendingAnalytics(@Param("userId") UUID userId);

    // =====================================================================
    // Find transactions by category for a user
    // =====================================================================
    @Query("SELECT t FROM Transaction t " +
            "WHERE t.userId = :userId AND t.categoryId = :categoryId " +
            "ORDER BY t.transactionDate DESC")
    List<Transaction> findTransactionsByUserAndCategory(
            @Param("userId") UUID userId,
            @Param("categoryId") UUID categoryId);

    // =====================================================================
    // Find spending by category (total)
    // =====================================================================
    @Query("SELECT t.categoryId, SUM(ABS(t.amount)) as totalAmount, COUNT(t) as count " +
            "FROM Transaction t " +
            "WHERE t.userId = :userId AND t.amount < 0 " +
            "GROUP BY t.categoryId " +
            "ORDER BY totalAmount DESC")
    List<Object[]> findSpendingByCategory(@Param("userId") UUID userId);

    // =====================================================================
    // Find top merchants
    // =====================================================================
    @Query("SELECT t.merchantName, SUM(ABS(t.amount)) as totalAmount, COUNT(t) as count " +
            "FROM Transaction t " +
            "WHERE t.userId = :userId AND t.amount < 0 " +
            "GROUP BY t.merchantName " +
            "ORDER BY totalAmount DESC")
    List<Object[]> findTopMerchants(@Param("userId") UUID userId);

    // =====================================================================
    // Find income vs expenses
    // =====================================================================
    @Query("SELECT " +
            "CASE WHEN t.amount > 0 THEN 'INCOME' ELSE 'EXPENSE' END as type, " +
            "SUM(t.amount) as total, " +
            "COUNT(t) as count " +
            "FROM Transaction t " +
            "WHERE t.userId = :userId " +
            "GROUP BY CASE WHEN t.amount > 0 THEN 'INCOME' ELSE 'EXPENSE' END")
    List<Object[]> findIncomeVsExpenses(@Param("userId") UUID userId);
    */
    /**
     * Find all transactions for a user with pagination.
     */
    Page<Transaction> findByUserIdOrderByTransactionDateDesc(UUID userId, Pageable pageable);

    /**
     * Find transactions by user and date range.
     */
    @Query("SELECT t FROM Transaction t " +
            "WHERE t.userId = :userId " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate " +
            "ORDER BY t.transactionDate DESC")
    Page<Transaction> findByUserAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    /**
     * Find transactions by user and category.
     */
    @Query("SELECT t FROM Transaction t " +
            "WHERE t.userId = :userId " +
            "AND t.categoryId = :categoryId " +
            "ORDER BY t.transactionDate DESC")
    List<Transaction> findByUserAndCategory(
            @Param("userId") UUID userId,
            @Param("categoryId") UUID categoryId);

    /**
     * Find transactions by user, category, and date range.
     */
    @Query("SELECT t FROM Transaction t " +
            "WHERE t.userId = :userId " +
            "AND t.categoryId = :categoryId " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate " +
            "ORDER BY t.transactionDate DESC")
    List<Transaction> findByUserCategoryAndDateRange(
            @Param("userId") UUID userId,
            @Param("categoryId") UUID categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Search transactions by merchant name (case-insensitive).
     */
    @Query("SELECT t FROM Transaction t " +
            "WHERE t.userId = :userId " +
            "AND LOWER(t.merchantName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "ORDER BY t.transactionDate DESC")
    Page<Transaction> searchByMerchant(
            @Param("userId") UUID userId,
            @Param("query") String query,
            Pageable pageable);

    /**
     * Search transactions by description (case-insensitive).
     */
    @Query("SELECT t FROM Transaction t " +
            "WHERE t.userId = :userId " +
            "AND (LOWER(t.merchantName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(t.rawDescription) LIKE LOWER(CONCAT('%', :query, '%'))) " +
            "ORDER BY t.transactionDate DESC")
    Page<Transaction> searchByMerchantOrDescription(
            @Param("userId") UUID userId,
            @Param("query") String query,
            Pageable pageable);

    /**
     * Find transactions from a specific statement.
     */
    List<Transaction> findByStatementIdOrderByTransactionDateDesc(UUID statementId);

    /**
     * Count transactions by statement.
     */
    Long countByStatementId(UUID statementId);

    /**
     * Find manual transactions.
     */
    List<Transaction> findByUserIdAndIsManualTrueOrderByTransactionDateDesc(UUID userId);

    /**
     * Find recurring transactions.
     */
    List<Transaction> findByUserIdAndIsRecurringTrueOrderByTransactionDateDesc(UUID userId);

    /**
     * Get total income for a user in a date range.
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.userId = :userId " +
            "AND t.amount > 0 " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalIncome(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get total expenses for a user in a date range.
     */
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.userId = :userId " +
            "AND t.amount < 0 " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalExpenses(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get spending by category.
     */
    @Query("SELECT t.categoryId, t.categoryName, SUM(ABS(t.amount)) as totalAmount, COUNT(t) as count " +
            "FROM Transaction t " +
            "WHERE t.userId = :userId " +
            "AND t.amount < 0 " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY t.categoryId, t.categoryName " +
            "ORDER BY totalAmount DESC")
    List<Object[]> getSpendingByCategory(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get top merchants by spending.
     */
    @Query("SELECT t.merchantName, SUM(ABS(t.amount)) as totalAmount, COUNT(t) as count " +
            "FROM Transaction t " +
            "WHERE t.userId = :userId " +
            "AND t.amount < 0 " +
            "AND t.transactionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY t.merchantName " +
            "ORDER BY totalAmount DESC")
    List<Object[]> getTopMerchants(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("limit") int limit);

    /**
     * Get average spending by category per month.
     */
    @Query("SELECT " +
            "CONCAT(CAST(YEAR(t.transactionDate) as string), '-', LPAD(CAST(MONTH(t.transactionDate) as string), 2, '0')) as yearMonth, " +
            "t.categoryId, t.categoryName, " +
            "AVG(ABS(t.amount)) as avgAmount " +
            "FROM Transaction t " +
            "WHERE t.userId = :userId " +
            "AND t.amount < 0 " +
            "GROUP BY YEAR(t.transactionDate), MONTH(t.transactionDate), t.categoryId, t.categoryName " +
            "ORDER BY yearMonth DESC, avgAmount DESC")
    List<Object[]> getAverageSpendingByMonthCategory(@Param("userId") UUID userId);

    /**
     * Find transactions with low confidence scores (LLM classification uncertainty).
     */
    @Query("SELECT t FROM Transaction t " +
            "WHERE t.userId = :userId " +
            "AND t.confidenceScore < :threshold " +
            "AND t.isManual = false " +
            "ORDER BY t.confidenceScore ASC")
    List<Transaction> findLowConfidenceTransactions(
            @Param("userId") UUID userId,
            @Param("threshold") Float threshold);

    /**
     * Get all transactions for a user in a month (for monthly analysis).
     */
    @Query("SELECT t FROM Transaction t " +
            "WHERE t.userId = :userId " +
            "AND YEAR(t.transactionDate) = :year " +
            "AND MONTH(t.transactionDate) = :month " +
            "ORDER BY t.transactionDate DESC")
    List<Transaction> findByUserAndYearMonth(
            @Param("userId") UUID userId,
            @Param("year") Integer year,
            @Param("month") Integer month);
}