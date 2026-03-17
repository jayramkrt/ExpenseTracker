package com.rotopay.expensetracker.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "processing_queue", indexes = {
        @Index(name = "idx_queue_status", columnList = "status"),
        @Index(name = "idx_queue_statement", columnList = "statement_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessingQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull(message = "Statement ID cannot be null")
    @Column(name = "statement_id", nullable = false)
    private UUID statementId;

    @NotNull(message = "User ID cannot be null")
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotBlank(message = "Job type cannot be blank")
    @Column(name = "job_type", nullable = false, length = 100)
    private String jobType;

    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private String status = "pending";

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "max_retries", nullable = false)
    @Builder.Default
    private Integer maxRetries = 3;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public boolean isPending() {
        return "pending".equals(this.status);
    }

    public boolean isInProgress() {
        return "in_progress".equals(this.status);
    }

    public boolean isCompleted() {
        return "completed".equals(this.status);
    }

    public boolean isFailed() {
        return "failed".equals(this.status);
    }

    public boolean canRetry() {
        return this.retryCount < this.maxRetries;
    }
}
