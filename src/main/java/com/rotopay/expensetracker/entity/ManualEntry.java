package com.rotopay.expensetracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "manual_entries", indexes = {
        @Index(name = "idx_manual_entries_user_date", columnList = "user_id,transaction_date"),
        @Index(name = "idx_manual_entries_category", columnList = "category_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManualEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "User ID cannot be null")
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotNull(message = "Transaction date cannot be null")
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "-999999.99", message = "Amount must be valid")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @NotBlank(message = "Description cannot be blank")
    @Column(nullable = false, length = 500)
    private String description;

    @NotNull(message = "Category ID cannot be null")
    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "transaction_type")
    private String transactionType; // income, expense, transfer

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

    public BigDecimal getAbsoluteAmount() {
        return this.amount.abs();
    }

}
