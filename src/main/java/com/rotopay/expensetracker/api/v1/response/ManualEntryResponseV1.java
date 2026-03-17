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
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ManualEntryResponseV1 {
    private UUID id;
    private LocalDate transactionDate;
    private BigDecimal amount;
    private String description;
    private UUID categoryId;
    private String categoryName;
    private String transactionType;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
