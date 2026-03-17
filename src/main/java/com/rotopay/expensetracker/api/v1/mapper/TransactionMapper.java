package com.rotopay.expensetracker.api.v1.mapper;

import com.rotopay.expensetracker.api.v1.response.TransactionResponseV1;
import com.rotopay.expensetracker.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionResponseV1 toResponse(Transaction entity){
        return TransactionResponseV1.builder()
                .id(entity.getId())
                .transactionDate(entity.getTransactionDate())
                .amount(entity.getAmount())
                .rawDescription(entity.getRawDescription())
                .merchantName(entity.getMerchantName())
                .referenceNumber(entity.getReferenceNumber())
                .categoryId(entity.getCategoryId())
                .categoryName(entity.getCategoryName())
                .subcategory(entity.getSubcategory())
                .confidenceScore(entity.getConfidenceScore())
                .llmReasoning(entity.getLlmReasoning())
                .transactionType(entity.getTransactionType())
                .isManual(entity.getIsManual())
                .isRecurring(entity.getIsRecurring())
                .recurringFrequency(entity.getRecurringFrequency())
                .notes(entity.getNotes())
                .statementId(entity.getStatementId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
