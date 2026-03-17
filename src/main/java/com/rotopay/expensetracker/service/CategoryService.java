package com.rotopay.expensetracker.service;

import com.rotopay.expensetracker.api.v1.mapper.CategoryMapper;
import com.rotopay.expensetracker.api.v1.response.CategoryResponseV1;
import com.rotopay.expensetracker.entity.Category;
import com.rotopay.expensetracker.api.common.exception.ResourceNotFoundException;
import com.rotopay.expensetracker.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for category operations.
 * Manages transaction categories.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {


    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    /**
     * Get all active categories.
     */
    @Transactional(readOnly = true)
    public List<CategoryResponseV1> getAllCategories() {
        log.debug("Fetching all categories");

        List<Category> categories = categoryRepository.findAllActive();
        return categories.stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get a specific category by ID.
     */
    @Transactional(readOnly = true)
    public CategoryResponseV1 getCategory(UUID categoryId) {
        log.debug("Fetching category: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + categoryId));

        return categoryMapper.toResponse(category);
    }

    /**
     * Get system categories only.
     */
    @Transactional(readOnly = true)
    public List<CategoryResponseV1> getSystemCategories() {
        log.debug("Fetching system categories");

        List<Category> categories = categoryRepository.findAllSystemCategories();
        return categories.stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Find category by name (internal use).
     */
    @Transactional(readOnly = true)
    public Category findByName(String name) {
        return categoryRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + name));
    }

}
