package com.rotopay.expensetracker.service;

import com.rotopay.expensetracker.api.v1.response.AnalyticsResponseV1;
import com.rotopay.expensetracker.api.v1.response.CategoryBreakdownResponseV1;
import com.rotopay.expensetracker.api.v1.response.MonthlyTrendResponseV1;
import com.rotopay.expensetracker.repository.AnalyticsCacheRepository;
import com.rotopay.expensetracker.repository.ManualEntryRepository;
import com.rotopay.expensetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for analytics and reporting.
 * Provides spending insights, trends, and analysis.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final TransactionRepository transactionRepository;
    private final ManualEntryRepository manualEntryRepository;
    private final AnalyticsCacheRepository cacheRepository;

    /**
     * Get overall analytics overview.
     */
    @Transactional(readOnly = true)
    public AnalyticsResponseV1 getOverview(UUID userId, LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching analytics overview for user: {}", userId);

        // Set default date range (last 30 days)
        if (startDate == null || endDate == null) {
            endDate = LocalDate.now();
            startDate = endDate.minusDays(30);
        }

        // Get transaction totals
        BigDecimal totalIncome = transactionRepository.getTotalIncome(userId, startDate, endDate);
        BigDecimal totalExpenses = transactionRepository.getTotalExpenses(userId, startDate, endDate);

        // Get manual entry totals
        BigDecimal manualIncome = manualEntryRepository.getTotalIncomeManual(userId, startDate, endDate);
        BigDecimal manualExpenses = manualEntryRepository.getTotalExpensesManual(userId, startDate, endDate);

        // Combine totals
        BigDecimal combinedIncome = totalIncome.add(manualIncome);
        BigDecimal combinedExpenses = totalExpenses.add(manualExpenses);
        BigDecimal netBalance = combinedIncome.add(combinedExpenses);

        AnalyticsResponseV1 response = AnalyticsResponseV1.builder()
                .totalIncome(combinedIncome)
                .totalExpenses(combinedExpenses)
                .netBalance(netBalance)
                .build();

        log.debug("Analytics overview calculated for user: {}", userId);
        return response;
    }

    /**
     * Get spending breakdown by category.
     */
    @Transactional(readOnly = true)
    public List<CategoryBreakdownResponseV1> getCategoryBreakdown(
            UUID userId,
            LocalDate startDate,
            LocalDate endDate) {

        log.debug("Fetching category breakdown for user: {}", userId);

        if (startDate == null || endDate == null) {
            endDate = LocalDate.now();
            startDate = endDate.minusDays(30);
        }

        List<Object[]> results = transactionRepository.getSpendingByCategory(userId, startDate, endDate);
        BigDecimal totalExpenses = transactionRepository.getTotalExpenses(userId, startDate, endDate).abs();

        return results.stream()
                .map(row -> {
                    UUID categoryId = (UUID) row[0];
                    String categoryName = (String) row[1];
                    BigDecimal amount = (BigDecimal) row[2];
                    Long count = (Long) row[3];

                    BigDecimal percentage = totalExpenses.compareTo(BigDecimal.ZERO) > 0
                            ? amount.divide(totalExpenses, 4, java.math.RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            : BigDecimal.ZERO;

                    return CategoryBreakdownResponseV1.builder()
                            .categoryId(categoryId)
                            .categoryName(categoryName)
                            .totalAmount(amount)
                            .percentage(percentage)
                            .transactionCount(count.intValue())
                            .averagePerTransaction(amount.divide(BigDecimal.valueOf(count), 2, java.math.RoundingMode.HALF_UP))
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Get monthly spending trends.
     */
    @Transactional(readOnly = true)
    public List<MonthlyTrendResponseV1> getMonthlyTrends(UUID userId, Integer months) {
        log.debug("Fetching monthly trends for user: {} for {} months", userId, months);

        List<MonthlyTrendResponseV1> trends = new ArrayList<>();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months);

        for (int i = months - 1; i >= 0; i--) {
            LocalDate monthStart = endDate.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

            BigDecimal income = transactionRepository.getTotalIncome(userId, monthStart, monthEnd);
            BigDecimal expenses = transactionRepository.getTotalExpenses(userId, monthStart, monthEnd);

            MonthlyTrendResponseV1 trend = MonthlyTrendResponseV1.builder()
                    .month(YearMonth.from(monthStart))
                    .income(income)
                    .expenses(expenses)
                    .netBalance(income.add(expenses))
                    .build();

            trends.add(trend);
        }

        return trends;
    }

    /**
     * Get top merchants by spending.
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTopMerchants(
            UUID userId,
            Integer limit,
            LocalDate startDate,
            LocalDate endDate) {

        log.debug("Fetching top merchants for user: {}", userId);

        if (startDate == null || endDate == null) {
            endDate = LocalDate.now();
            startDate = endDate.minusDays(30);
        }

        List<Object[]> results = transactionRepository.getTopMerchants(userId, startDate, endDate, limit);

        return results.stream()
                .map(row -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("merchantName", row[0]);
                    map.put("totalSpending", row[1]);
                    map.put("transactionCount", row[2]);
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * Detect recurring transactions (subscriptions, regular expenses).
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getRecurringTransactions(UUID userId) {
        log.debug("Fetching recurring transactions for user: {}", userId);

        // Find transactions marked as recurring
        var recurringTransactions = transactionRepository.findByUserIdAndIsRecurringTrueOrderByTransactionDateDesc(userId);

        return recurringTransactions.stream()
                .map(t -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("transactionId", t.getId());
                    map.put("merchantName", t.getMerchantName());
                    map.put("amount", t.getAmount());
                    map.put("frequency", t.getRecurringFrequency());
                    map.put("categoryName", t.getCategoryName());
                    map.put("lastOccurrence", t.getTransactionDate());
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * Detect spending anomalies (unusual transactions).
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAnomalies(UUID userId, Double threshold) {
        log.debug("Fetching anomalies for user: {} with threshold: {}", userId, threshold);

        // Get all transactions for user (last 90 days for context)
        LocalDate ninetyDaysAgo = LocalDate.now().minusDays(90);
        List<Map<String, Object>> anomalies = new ArrayList<>();

        // Group by category and calculate averages
        Map<UUID, List<BigDecimal>> categoryAmounts = new HashMap<>();
        Map<UUID, String> categoryNames = new HashMap<>();

        transactionRepository.findByUserAndDateRange(
                userId, ninetyDaysAgo, LocalDate.now(),
                org.springframework.data.domain.PageRequest.of(0, 1000)
        ).forEach(t -> {
            if (t.getCategoryId() != null) {
                categoryAmounts.computeIfAbsent(t.getCategoryId(), k -> new ArrayList<>())
                        .add(t.getAmount().abs());
                categoryNames.put(t.getCategoryId(), t.getCategoryName());
            }
        });

        // Detect anomalies
        for (Map.Entry<UUID, List<BigDecimal>> entry : categoryAmounts.entrySet()) {
            List<BigDecimal> amounts = entry.getValue();
            if (amounts.size() < 3) continue; // Need at least 3 transactions

            BigDecimal average = amounts.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(amounts.size()), 2, java.math.RoundingMode.HALF_UP);

            for (BigDecimal amount : amounts) {
                double deviation = amount.doubleValue() / average.doubleValue();
                if (deviation > threshold) {
                    Map<String, Object> anomaly = new HashMap<>();
                    anomaly.put("categoryId", entry.getKey());
                    anomaly.put("categoryName", categoryNames.get(entry.getKey()));
                    anomaly.put("amount", amount);
                    anomaly.put("average", average);
                    anomaly.put("deviation", deviation);
                    anomalies.add(anomaly);
                }
            }
        }

        return anomalies;
    }

    /**
     * Export complete analytics data.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCompleteExport(UUID userId, LocalDate startDate, LocalDate endDate) {
        log.debug("Exporting analytics for user: {}", userId);

        Map<String, Object> export = new HashMap<>();

        export.put("summary", getOverview(userId, startDate, endDate));
        export.put("categoryBreakdown", getCategoryBreakdown(userId, startDate, endDate));
        export.put("monthlyTrends", getMonthlyTrends(userId, 12));
        export.put("topMerchants", getTopMerchants(userId, 10, startDate, endDate));
        export.put("recurringTransactions", getRecurringTransactions(userId));

        return export;
    }
}
