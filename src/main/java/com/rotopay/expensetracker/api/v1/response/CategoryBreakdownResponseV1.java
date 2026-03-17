package com.rotopay.expensetracker.api.v1.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryBreakdownResponseV1 {
    private UUID categoryId;
    private String categoryName;
    private String icon;
    private String color;

    private BigDecimal totalAmount;
    private BigDecimal percentage;
    private Integer transactionCount;
    private BigDecimal averagePerTransaction;
}