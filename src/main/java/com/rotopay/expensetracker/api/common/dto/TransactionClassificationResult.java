package com.rotopay.expensetracker.api.common.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of LLM classification of a transaction.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionClassificationResult {
    private String category;
    private Float confidenceScore;
    private String reasoning;
}
