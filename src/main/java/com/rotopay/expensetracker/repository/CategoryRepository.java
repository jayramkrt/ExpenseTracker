package com.rotopay.expensetracker.repository;

import com.rotopay.expensetracker.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Category entity.
 * Handles category retrieval and management.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    /**
     * Find category by name (exact match).
     */
    Optional<Category> findByName(String name);

    /**
     * Find category by name (case-insensitive).
     */
    Optional<Category> findByNameIgnoreCase(String name);

    /**
     * Get all active system categories.
     */
    @Query("SELECT c FROM Category c WHERE c.isSystem = true AND c.isActive = true ORDER BY c.name")
    List<Category> findAllSystemCategories();

    /**
     * Get all active categories (system and user-created).
     */
    @Query("SELECT c FROM Category c WHERE c.isActive = true ORDER BY c.isSystem DESC, c.name")
    List<Category> findAllActive();

    /**
     * Check if category exists by name.
     */
    boolean existsByName(String name);
}
