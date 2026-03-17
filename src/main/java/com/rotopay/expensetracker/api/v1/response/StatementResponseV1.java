package com.rotopay.expensetracker.api.v1.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatementResponseV1 {

    private UUID id;
    private String fileName;
    private String bankName;
    private String accountType;
    private String accountLastFour;
    private LocalDate statementPeriodStart;
    private LocalDate statementPeriodEnd;
    private String processingStatus; // pending, processing, completed, failed
    private String errorMessage;
    private Integer transactionCount;
    private LocalDateTime uploadedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
