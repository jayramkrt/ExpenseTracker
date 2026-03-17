package com.rotopay.expensetracker.api.v1.mapper;

import com.rotopay.expensetracker.api.v1.response.StatementResponseV1;
import com.rotopay.expensetracker.entity.BankStatement;
import org.springframework.stereotype.Component;

@Component
public class StatementMapper {

    public StatementResponseV1 toResponse(BankStatement bankStatement){
        return StatementResponseV1.builder()
                .id(bankStatement.getId())
                .fileName(bankStatement.getFileName())
                .bankName(bankStatement.getBankName())
                .accountType(bankStatement.getAccountType())
                .accountLastFour(bankStatement.getAccountLastFour())
                .statementPeriodStart(bankStatement.getStatementPeriodStart())
                .statementPeriodEnd(bankStatement.getStatementPeriodEnd())
                .processingStatus(bankStatement.getProcessingStatus())
                .errorMessage(bankStatement.getErrorMessage())
                .transactionCount(bankStatement.getTransactionCount())
                .updatedAt(bankStatement.getUpdatedAt())
                .createdAt(bankStatement.getCreatedAt())
                .uploadedAt(bankStatement.getUploadedAt())
                .build();
    }
}
