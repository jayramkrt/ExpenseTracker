package com.rotopay.expensetracker.repository;

import com.rotopay.expensetracker.entity.AnalyticsCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for AnalyticsCache entity.
 * Manages cached analytics data for performance optimization.
 */
@Repository
public interface AnalyticsCacheRepository extends JpaRepository<AnalyticsCache, UUID> {

    /**
     * Find cache entry by user and cache key.
     */
    Optional<AnalyticsCache> findByUserIdAndCacheKey(UUID userId, String cacheKey);

    /**
     * Delete cache entries by cache key.
     */
    void deleteByCacheKey(String cacheKey);

    /**
     * Delete cache entries by user ID.
     */
    void deleteByUserId(UUID userId);

    /**
     * Find expired cache entries for cleanup.
     */
    @Query("SELECT ac FROM AnalyticsCache ac " +
            "WHERE ac.expiresAt IS NOT NULL " +
            "AND ac.expiresAt < :now")
    List<AnalyticsCache> findExpiredEntries(@Param("now") LocalDateTime now);

    /**
     * Find valid (not expired) cache entries by user.
     */
    @Query("SELECT ac FROM AnalyticsCache ac " +
            "WHERE ac.userId = :userId " +
            "AND (ac.expiresAt IS NULL OR ac.expiresAt > :now)")
    List<AnalyticsCache> findValidEntriesByUser(
            @Param("userId") UUID userId,
            @Param("now") LocalDateTime now);
}

