package com.rotopay.expensetracker.api.v1.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.YearMonth;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MonthlyTrendResponseV1 {

    private YearMonth month;
    private BigDecimal income;
    private BigDecimal expenses;
    private BigDecimal netBalance;
    private Integer transactionCount;
}