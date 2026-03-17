package com.rotopay.expensetracker.api.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Raw transaction extracted from PDF or statement.
 * Before LLM classification and entity creation.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RawTransaction {
    private String date;
    private String amount;
    private String description;
    private String merchant;
    private String referenceNumber;
}
