package com.rotopay.expensetracker.api.v1.controller;


import com.rotopay.expensetracker.api.v1.response.AnalyticsResponseV1;
import com.rotopay.expensetracker.api.v1.response.CategoryBreakdownResponseV1;
import com.rotopay.expensetracker.api.v1.response.MonthlyTrendResponseV1;
import com.rotopay.expensetracker.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
public class AnalyticsControllerV1 {

    private final AnalyticsService analyticsService;

    /**
     * Get overall analytics for the authenticated user.
     * Includes total income, expenses, net balance, and monthly trend.
     */
    @GetMapping("/overview")
    public ResponseEntity<AnalyticsResponseV1> getOverview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestHeader("Authorization") String userId) {

        log.debug("Fetching analytics overview for user: {}", userId);
        AnalyticsResponseV1 overview = analyticsService.getOverview(
                UUID.fromString(userId), startDate, endDate);

        return ResponseEntity.ok(overview);
    }

    /**
     * Get spending breakdown by category.
     * Useful for pie/donut chart visualization.
     */
    @GetMapping("/category-breakdown")
    public ResponseEntity<List<CategoryBreakdownResponseV1>> getCategoryBreakdown(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestHeader("Authorization") String userId) {

        log.debug("Fetching category breakdown for user: {}", userId);
        List<CategoryBreakdownResponseV1> breakdown = analyticsService.getCategoryBreakdown(
                UUID.fromString(userId), startDate, endDate);

        return ResponseEntity.ok(breakdown);
    }

    /**
     * Get monthly spending trends.
     * Returns data for the last 12 months by default.
     */
    @GetMapping("/monthly-trends")
    public ResponseEntity<List<MonthlyTrendResponseV1>> getMonthlyTrends(
            @RequestParam(required = false, defaultValue = "12") Integer months,
            @RequestHeader("Authorization") String userId) {

        log.debug("Fetching monthly trends for user: {}", userId);
        List<MonthlyTrendResponseV1> trends = analyticsService.getMonthlyTrends(
                UUID.fromString(userId), months);

        return ResponseEntity.ok(trends);
    }

    /**
     * Get top merchants by spending.
     */
    @GetMapping("/top-merchants")
    public ResponseEntity<List<Map<String, Object>>> getTopMerchants(
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestHeader("Authorization") String userId) {

        log.debug("Fetching top merchants for user: {}", userId);
        List<Map<String, Object>> merchants = analyticsService.getTopMerchants(
                UUID.fromString(userId), limit, startDate, endDate);

        return ResponseEntity.ok(merchants);
    }

    /**
     * Detect recurring transactions (subscriptions, regular expenses).
     */
    @GetMapping("/recurring-transactions")
    public ResponseEntity<List<Map<String, Object>>> getRecurringTransactions(
            @RequestHeader("Authorization") String userId) {

        log.debug("Fetching recurring transactions for user: {}", userId);
        List<Map<String, Object>> recurring = analyticsService.getRecurringTransactions(
                UUID.fromString(userId));

        return ResponseEntity.ok(recurring);
    }

    /**
     * Detect spending anomalies (unusual transactions compared to average).
     */
    @GetMapping("/anomalies")
    public ResponseEntity<List<Map<String, Object>>> getAnomalies(
            @RequestParam(required = false, defaultValue = "2.0") Double threshold,
            @RequestHeader("Authorization") String userId) {

        log.debug("Fetching anomalies for user: {}", userId);
        List<Map<String, Object>> anomalies = analyticsService.getAnomalies(
                UUID.fromString(userId), threshold);

        return ResponseEntity.ok(anomalies);
    }

    /**
     * Export analytics data as JSON (for reports, exports, etc.).
     */
    @GetMapping("/export")
    public ResponseEntity<Map<String, Object>> exportAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestHeader("Authorization") String userId) {

        log.debug("Exporting analytics for user: {}", userId);
        Map<String, Object> export = analyticsService.getCompleteExport(
                UUID.fromString(userId), startDate, endDate);

        return ResponseEntity.ok(export);
    }

}
