package com.Enigmazer.todo_app.service.category;

import com.Enigmazer.todo_app.dto.category.CategoryCreationRequest;
import com.Enigmazer.todo_app.dto.category.CategoryResponseDTO;
import com.Enigmazer.todo_app.exception.CustomExceptions.ResourceNotFoundException;
import com.Enigmazer.todo_app.model.Category;

import java.util.List;

/**
 * Service interface for managing categories in the todo application.
 * Provides operations for creating, reading, updating, and deleting categories,
 * as well as retrieving category statistics.
 */
public interface CategoryService {

    /**
     * Creates a new category with the provided details.
     *
     * @param category The category creation request containing the category details
     * @return A DTO containing the created category's information
     */
    CategoryResponseDTO createCategory(CategoryCreationRequest category);

    /**
     * Retrieves all categories available to the current user.
     * This includes both user-specific categories and global categories.
     *
     * @return A list of category DTOs
     */
    List<CategoryResponseDTO> getCategories();

    /**
     * Retrieves a category by its ID if it's accessible to the current user.
     *
     * @param categoryId The ID of the category to retrieve
     * @return The category entity if found and accessible
     * @throws ResourceNotFoundException if the category is not found or not accessible
     */
    Category getCategoryById(Long categoryId);

    /**
     * Deletes a category by its ID if it belongs to the current user.
     * Note: Only user-specific categories can be deleted.
     *
     * @param categoryId The ID of the category to delete
     * @throws ResourceNotFoundException if the category is not found
     * @throws IllegalArgumentException if the category is not owned by the current user
     */
    void deleteCategory(Long categoryId);

    /**
     * Counts the total number of categories available to the current user.
     * This includes both user-specific categories and global categories.
     *
     * @return The total count of accessible categories
     */
    Integer countCategories();

    /**
     * Updates an existing category with new details.
     * Only the owner of the category can update it.
     *
     * @param categoryId The ID of the category to update
     * @param category The updated category details
     * @return A DTO containing the updated category's information
     * @throws ResourceNotFoundException if the category is not found
     * @throws IllegalArgumentException if the category is not owned by the current user
     */
    CategoryResponseDTO updateCategory(Long categoryId, CategoryCreationRequest category);
}