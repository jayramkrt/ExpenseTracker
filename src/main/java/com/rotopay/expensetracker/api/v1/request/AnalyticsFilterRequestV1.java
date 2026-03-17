
package com.rotopay.expensetracker.api.v1.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Analytics Filter Request - Version 1
 * Used to filter and retrieve analytics data with various parameters.
 *
 * Query Parameters or Request Body:
 * - startDate: From date (optional, defaults to 30 days ago)
 * - endDate: To date (optional, defaults to today)
 * - categoryId: Filter by specific category (optional)
 * - months: Number of months for trends (optional, defaults to 12)
 * - limit: Limit for top merchants (optional, defaults to 10)
 * - threshold: Threshold for anomaly detection (optional, defaults to 2.0)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalyticsFilterRequestV1 {

    /**
     * Start date for analytics period.
     * If not provided, defaults to 30 days before endDate.
     */
    private LocalDate startDate;

    /**
     * End date for analytics period.
     * If not provided, defaults to today.
     */
    private LocalDate endDate;

    /**
     * Filter analytics by specific category.
     * Optional - if not provided, includes all categories.
     */
    private UUID categoryId;

    /**
     * Number of months for monthly trends analysis.
     * Optional - defaults to 12 months.
     * Must be between 1 and 60.
     */
    @Min(value = 1, message = "Months must be at least 1")
    private Integer months;

    /**
     * Limit for top merchants list.
     * Optional - defaults to 10.
     * Must be between 1 and 100.
     */
    @Min(value = 1, message = "Limit must be at least 1")
    private Integer limit;

    /**
     * Threshold for anomaly detection.
     * Transactions with spending deviation > threshold are flagged.
     * Optional - defaults to 2.0 (2x average).
     * Must be greater than 1.0.
     */
    @Min(value = 1, message = "Threshold must be greater than 1")
    private Double threshold;

    /**
     * Include recurring transactions in analysis.
     * Optional - defaults to true.
     */
    @Builder.Default
    private Boolean includeRecurring = true;

    /**
     * Include manual entries in analysis.
     * Optional - defaults to true.
     */
    @Builder.Default
    private Boolean includeManualEntries = true;

    /**
     * Include statement transactions in analysis.
     * Optional - defaults to true.
     */
    @Builder.Default
    private Boolean includeStatementTransactions = true;

    /**
     * Validate and get effective parameters.
     * Provides defaults for optional fields.
     */
    public void applyDefaults() {
        // Set end date to today if not provided
        if (this.endDate == null) {
            this.endDate = LocalDate.now();
        }

        // Set start date to 30 days ago if not provided
        if (this.startDate == null) {
            this.startDate = this.endDate.minusDays(30);
        }

        // Set default months
        if (this.months == null) {
            this.months = 12;
        } else if (this.months < 1 || this.months > 60) {
            this.months = 12; // Reset to default if invalid
        }

        // Set default limit
        if (this.limit == null) {
            this.limit = 10;
        } else if (this.limit < 1 || this.limit > 100) {
            this.limit = 10; // Reset to default if invalid
        }

        // Set default threshold
        if (this.threshold == null) {
            this.threshold = 2.0;
        } else if (this.threshold <= 1.0) {
            this.threshold = 2.0; // Reset to default if invalid
        }
    }

    /**
     * Validate date range.
     */
    public boolean isValidDateRange() {
        if (this.startDate == null || this.endDate == null) {
            return false;
        }
        return this.startDate.isBefore(this.endDate) || this.startDate.isEqual(this.endDate);
    }

    /**
     * Get number of days in the date range.
     */
    public long getDaysDifference() {
        if (this.startDate == null || this.endDate == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(this.startDate, this.endDate);
    }
}