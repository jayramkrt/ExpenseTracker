package com.rotopay.expensetracker.api.v1.mapper;

import com.rotopay.expensetracker.api.v1.response.ManualEntryResponseV1;
import com.rotopay.expensetracker.entity.ManualEntry;
import org.springframework.stereotype.Component;

@Component
public class ManualEntryMapper {

    public ManualEntryResponseV1 toResponse(ManualEntry entity){
        return ManualEntryResponseV1.builder()
                .id(entity.getId())
                .transactionDate(entity.getTransactionDate())
                .amount(entity.getAmount())
                .description(entity.getDescription())
                .categoryId(entity.getCategoryId())
                .transactionType(entity.getTransactionType())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
