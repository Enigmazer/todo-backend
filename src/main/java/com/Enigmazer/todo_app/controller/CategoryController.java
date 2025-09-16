package com.Enigmazer.todo_app.controller;

import com.Enigmazer.todo_app.dto.category.CategoryCreationRequest;
import com.Enigmazer.todo_app.dto.category.CategoryResponseDTO;
import com.Enigmazer.todo_app.service.category.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
 *     <li>Update a category</li>
 *     <li>Delete a category</li>
 *     <li>Get total categories count</li>
 * </ul>
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Add a new category for the current user.
     *
     * @param category The category creation request
     * @return The saved category
     */
    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(@Valid @RequestBody CategoryCreationRequest category) {
        log.info("Category creation request received for name: {}", category.getName());
        CategoryResponseDTO saved = categoryService.createCategory(category);
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

    /**
     *  Used for updating the name of the category
     *
     * @param categoryId id if the category that we have to update
     * @param category  contains the new name for the category
     * @return  updated category
     */
    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(@PathVariable long categoryId,
                                                              @Valid @RequestBody CategoryCreationRequest category){
        return ResponseEntity.ok(categoryService.updateCategory(categoryId,category));
    }

    /**
     * Delete a category by its ID (must belong to current user).
     *
     * @param categoryId ID of the category to delete
     * @return HTTP 200
     */
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long categoryId) {
        log.info("Request to delete category with ID: {}", categoryId);
        categoryService.deleteCategory(categoryId);
        log.info("Category with ID: {} deleted successfully", categoryId);
        return ResponseEntity.ok().build();
    }

    /**
     * used for getting the total categories count of the user
     * including global categories
     *
     * @return count of the categories
     */
    @GetMapping("/count")
    public ResponseEntity<Integer> countCategories(){
        return ResponseEntity.ok(categoryService.countCategories());
    }
}
