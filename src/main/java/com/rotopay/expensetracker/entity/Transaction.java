package com.rotopay.expensetracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_transactions_user_date", columnList = "user_id,transaction_date"),
        @Index(name = "idx_transactions_category", columnList = "category_id"),
        @Index(name = "idx_transactions_merchant", columnList = "merchant_name"),
        @Index(name = "idx_transactions_is_manual", columnList = "is_manual"),
        @Index(name = "idx_transactions_recurring", columnList = "is_recurring")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "User ID cannot be null")
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "statement_id")
    private UUID statementId;


    @NotNull(message = "Transaction date cannot be null")
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "-999999.99", message = "Amount must be valid")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "raw_description", length = 500)
    private String rawDescription;

    @Column(name = "merchant_name")
    private String merchantName;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "subcategory")
    private String subcategory;

    @Column(name = "confidence_score")
    @Builder.Default
    private Float confidenceScore = 0.0f;

    @Column(name = "llm_reasoning", columnDefinition = "TEXT")
    private String llmReasoning;

    @Column(name = "transaction_type")
    private String transactionType; // debit, credit, transfer

    @Column(name = "is_manual", nullable = false)
    @Builder.Default
    private Boolean isManual = false;

    @Column(name = "is_recurring", nullable = false)
    @Builder.Default
    private Boolean isRecurring = false;

    @Column(name = "recurring_frequency")
    private String recurringFrequency;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public boolean isExpense() {
        return this.amount.compareTo(BigDecimal.ZERO) < 0;
    }

    public boolean isIncome() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean hasHighConfidence() {
        return this.confidenceScore != null && this.confidenceScore >= 0.8f;
    }

    public BigDecimal getAbsoluteAmount() {
        return this.amount.abs();
    }
}
