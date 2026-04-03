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

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(@Valid @RequestBody CategoryCreationRequest category) {
        log.info("Category creation request received for name: {}", category.getName());
        CategoryResponseDTO saved = categoryService.createCategory(category);
        log.info("Category created successfully with ID: {}", saved.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getCategories() {
        log.info("Fetching categories for current user");
        List<CategoryResponseDTO> categories = categoryService.getCategories();
        log.info("Fetched {} categories", categories.size());
        return ResponseEntity.ok(categories);
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(@PathVariable long categoryId,
                                                              @Valid @RequestBody CategoryCreationRequest category){
        return ResponseEntity.ok(categoryService.updateCategory(categoryId,category));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long categoryId) {
        log.info("Request to delete category with ID: {}", categoryId);
        categoryService.deleteCategory(categoryId);
        log.info("Category with ID: {} deleted successfully", categoryId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> countCategories(){
        return ResponseEntity.ok(categoryService.countCategories());
    }
}
