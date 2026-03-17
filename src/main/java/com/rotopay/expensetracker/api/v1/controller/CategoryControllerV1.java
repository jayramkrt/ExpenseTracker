package com.rotopay.expensetracker.api.v1.controller;

import com.rotopay.expensetracker.api.v1.response.CategoryResponseV1;
import com.rotopay.expensetracker.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryControllerV1 {

    private final CategoryService categoryService;

    /**
     * Get all active categories (both system and user-created).
     * @return
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponseV1>> getAllCategories(){
        log.debug("Fetching all categories");
        List<CategoryResponseV1> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    /**
     * Get a specific category by ID.
     * @return
     */
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseV1> getCategory(@PathVariable UUID categoryId){
        log.debug("Fetching category: {}", categoryId);
        CategoryResponseV1 category = categoryService.getCategory(categoryId);
        return ResponseEntity.ok(category);
    }

    /**
     * Get system categories only (pre-defined by app).
     */
    @GetMapping("/system")
    public ResponseEntity<List<CategoryResponseV1>> getSystemCategories() {
        log.debug("Fetching system categories");
        List<CategoryResponseV1> categories = categoryService.getSystemCategories();
        return ResponseEntity.ok(categories);
    }
}
