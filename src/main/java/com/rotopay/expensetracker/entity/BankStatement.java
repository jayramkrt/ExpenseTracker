package com.rotopay.expensetracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bank_statements", indexes = {
        @Index(name = "idx_statements_user_id", columnList = "user_id"),
        @Index(name = "idx_statements_status", columnList = "processing_status"),
        @Index(name = "idx_statements_period", columnList = "statement_period_start,statement_period_end")
})
@Data
@Builder
public class BankStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "User ID cannot be null")
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotBlank(message="file name cannot be blank")
    @Column(nullable = false)
    private String fileName;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "account_type")
    private String accountType; // checking, savings, credit_card

    @Column(name = "account_last_four", length = 4)
    private String accountLastFour;

    @Column(name = "statement_period_start")
    private LocalDate statementPeriodStart;

    @Column(name = "statement_period_end")
    private LocalDate statementPeriodEnd;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "processing_status", nullable = false)
    @Builder.Default
    private String processingStatus = "pending"; // pending, processing, completed, failed

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "transaction_count")
    @Builder.Default
    private Integer transactionCount = 0;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


    public boolean isProcessing() {
        return "processing".equals(this.processingStatus);
    }

    public boolean isCompleted() {
        return "completed".equals(this.processingStatus);
    }

    public boolean isFailed() {
        return "failed".equals(this.processingStatus);
    }
}
