package com.rotopay.expensetracker.api.v1.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AnalyticsResponseV1 {
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netBalance;

    private BigDecimal averageMonthlyIncome;
    private BigDecimal averageMonthlyExpenses;

    private Integer transactionCount;
    private Integer manualEntryCount;

    private List<MonthlyTrendResponseV1> monthlyTrends;
    private List<CategoryBreakdownResponseV1> categoryBreakdown;
}
