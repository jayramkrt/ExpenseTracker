package com.rotopay.expensetracker.api.v1.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionResponseV1 {

    private UUID id;
    private LocalDate transactionDate;
    private BigDecimal amount;
    private String rawDescription;
    private String merchantName;
    private String referenceNumber;

    private UUID categoryId;
    private String categoryName;
    private String subcategory;
    private Float confidenceScore;
    private String llmReasoning;

    private String transactionType; // debit, credit, transfer
    private Boolean isManual;
    private Boolean isRecurring;
    private String recurringFrequency;
    private String notes;

    private UUID statementId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
