package com.Enigmazer.todo_app.controller;

import com.Enigmazer.todo_app.dto.category.CategoryCreationRequest;
import com.Enigmazer.todo_app.dto.category.CategoryResponseDTO;
import com.Enigmazer.todo_app.service.category.CategoryService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * CategoryController handles all operations related to task categories.
 * <p>
 * All endpoints are protected via JWT, and rely on {@link CategoryService}
 * to ensure the current user is scoped correctly.
 * </p>
 * Supported operations:
 * <ul>
 *     <li>Add a new category</li>
 *     <li>Get all categories</li>
 *     <li>Delete a category</li>
 * </ul>
 */
@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Add a new category for the current user.
     *
     * @param category The category creation request
     * @return The saved category
     */
    @PostMapping
    public ResponseEntity<CategoryResponseDTO> addCategory(@Valid @RequestBody CategoryCreationRequest category) {
        log.info("Category creation request received for name: {}", category.getName());
        CategoryResponseDTO saved = categoryService.addCategory(category);
        log.info("Category created successfully with ID: {}", saved.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Fetch all categories of the current user.
     *
     * @return List of categories
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getCategories() {
        log.info("Fetching categories for current user");
        List<CategoryResponseDTO> categories = categoryService.getCategories();
        log.info("Fetched {} categories", categories.size());
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/update/{categoryId}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(@PathVariable long categoryId,
                                                              @Valid @RequestBody CategoryCreationRequest category){
        return ResponseEntity.ok(categoryService.updateCategory(categoryId,category));
    }

    /**
     * Delete a category by its ID (must belong to current user).
     *
     * @param categoryId ID of the category to delete
     * @return HTTP 200 if deleted
     */
    @DeleteMapping("/delete/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long categoryId) {
        log.info("Request to delete category with ID: {}", categoryId);
        categoryService.deleteCategory(categoryId);
        log.info("Category with ID: {} deleted successfully", categoryId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/total")
    public ResponseEntity<Integer> totalCategoriesOfUser(){
        return ResponseEntity.ok(categoryService.totalCategoriesOfUser());
    }
}
