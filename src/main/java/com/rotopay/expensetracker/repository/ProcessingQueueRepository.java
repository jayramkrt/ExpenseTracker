package com.rotopay.expensetracker.repository;

import com.rotopay.expensetracker.entity.ProcessingQueue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for ProcessingQueue entity.
 * Manages async job queue for PDF extraction and LLM classification.
 */
@Repository
public interface ProcessingQueueRepository extends JpaRepository<ProcessingQueue, UUID> {

    /**
     * Find all pending jobs (FIFO order).
     */
    List<ProcessingQueue> findByStatusOrderByCreatedAtAsc(String status);

    /**
     * Find pending jobs by type.
     */
    List<ProcessingQueue> findByStatusAndJobTypeOrderByCreatedAtAsc(String status, String jobType);

    /**
     * Find jobs by statement ID.
     */
    List<ProcessingQueue> findByStatementIdOrderByCreatedAtDesc(UUID statementId);

    /**
     * Find in-progress jobs.
     */
    @Query("SELECT pq FROM ProcessingQueue pq " +
            "WHERE pq.status = 'in_progress' " +
            "ORDER BY pq.startedAt ASC")
    List<ProcessingQueue> findInProgressJobs();

    /**
     * Find failed jobs that can be retried.
     */
    @Query("SELECT pq FROM ProcessingQueue pq " +
            "WHERE pq.status = 'failed' " +
            "AND pq.retryCount < pq.maxRetries " +
            "ORDER BY pq.createdAt ASC")
    List<ProcessingQueue> findRetryableFailedJobs();

    /**
     * Count pending jobs.
     */
    Long countByStatus(String status);

    /**
     * Find the latest job for a statement.
     */
    Optional<ProcessingQueue> findFirstByStatementIdOrderByCreatedAtDesc(UUID statementId);

    /**
     * Find jobs by user.
     */
    List<ProcessingQueue> findByUserIdOrderByCreatedAtDesc(UUID userId);

    /**
     * Atomically claim a job: flips status pending → in_progress in a single
     * UPDATE statement so concurrent scheduler threads cannot double-claim.
     * Returns the number of rows updated (1 = claimed, 0 = already taken).
     */
    @Modifying
    @Transactional
    @Query("UPDATE ProcessingQueue pq " +
            "SET pq.status = 'in_progress', pq.startedAt = :now " +
            "WHERE pq.id = :id AND pq.status = 'pending'")
    int atomicClaimJob(UUID id, LocalDateTime now);
}

