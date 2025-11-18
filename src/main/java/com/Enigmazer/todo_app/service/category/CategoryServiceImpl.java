package com.Enigmazer.todo_app.service.category;

import com.Enigmazer.todo_app.dto.category.CategoryCreationRequest;
import com.Enigmazer.todo_app.dto.category.CategoryResponseDTO;
import com.Enigmazer.todo_app.exception.CustomExceptions.ResourceNotFoundException;
import com.Enigmazer.todo_app.mapper.CategoryMapper;
import com.Enigmazer.todo_app.model.Category;
import com.Enigmazer.todo_app.repository.CategoryRepository;
import com.Enigmazer.todo_app.service.JWTService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of the {@link CategoryService} interface.
 * <p>
 * This service manages operations related to categories,
 * including creation, fetching, and deletion of user-specific categories.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final JWTService jwtService;
    private final CategoryMapper categoryMapper;
    
    /**
     * Adds a new category for the currently authenticated user.
     *
     * @param category The category creation request containing the name
     * @return DTO of the saved category
     */
    @Override
    public CategoryResponseDTO createCategory(CategoryCreationRequest category) {
        log.info("Attempting to add category: {}", category.getName());

        Category newCategory = categoryRepository.save(
                Category.builder()
                        .name(category.getName())
                        .user(jwtService.getCurrentUser())
                        .build()
        );

        log.info("Category saved successfully with ID: {}", newCategory.getId());

        return categoryMapper.toDto(newCategory);
    }

    /**
     * Fetches all available categories for the current user.
     *
     * @return List of user-specific categories
     */
    @Override
    public List<CategoryResponseDTO> getCategories() {
        Long userId = jwtService.getCurrentUser().getId();
        log.info("Fetching categories for user ID: {}", userId);

        List<Category> categories = categoryRepository.findAvailableCategories(userId);
        log.info("{} categories fetched", categories.size());

        return categories.stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    /**
     * Fetch a specific category by ID and user ID.
     *
     * @param categoryId ID of the category
     * @return Category if exists and belongs to current user
     */
    @Override
    public Category getCategoryById(Long categoryId) {
        Long userId = jwtService.getCurrentUser().getId();
        log.info("Fetching category ID: {} for user ID: {}", categoryId, userId);

        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> {
                    log.error("No category found with ID: {} for user ID: {}", categoryId, userId);
                    return new ResourceNotFoundException("No category found with the given ID.");
                });
    }

    /**
     * Updates an existing category with new details.
     * Only the owner of the category can update it.
     * and global categories can not be updated by normal users.
     *
     * @param categoryId The ID of the category to update
     * @param category The updated category details
     * @return A DTO containing the updated category's information
     * @throws ResourceNotFoundException if the category is not found
     * @throws IllegalArgumentException if the category is not owned by the current user
     */
    @Override
    public CategoryResponseDTO updateCategory(Long categoryId,CategoryCreationRequest category){
        Category existingCategory = getCategoryById(categoryId);
        if (existingCategory.isGlobal()) {
            log.debug("Attempted to updates a global category (ID: {})", categoryId);
            throw new IllegalArgumentException("Global categories can't be updated");
        }
        existingCategory.setName(category.getName());
        return categoryMapper.toDto(categoryRepository.save(existingCategory));
    }

    /**
     * Deletes a category by ID if it's not marked as global.
     *
     * @param categoryId ID of the category to delete
     */
    @Override
    public void deleteCategory(Long categoryId) {
        log.info("Deletion request received for category ID: {}", categoryId);
        Category category = getCategoryById(categoryId);

        if (category.isGlobal()) {
            log.debug("Attempted to delete a global category (ID: {})", categoryId);
            throw new IllegalArgumentException("Global categories can't be deleted");
        }

        if (categoryRepository.countTasksByCategoryId(categoryId) > 0){
            log.debug("Attempted to delete a category (ID: {}) which belongs to a task.", categoryId);
            throw new IllegalArgumentException("Category can not be deleted because it belongs to a task.");
        }

        categoryRepository.delete(category);
        log.info("Category deleted successfully (ID: {})", categoryId);
    }

    /**
     * Counts the total number of categories available to the current user.
     * This includes both user-specific categories and global categories.
     *
     * @return The total count of accessible categories
     */
    @Override
    public Integer countCategories(){
        return categoryRepository.countCategories(jwtService.getCurrentUser().getId());
    }
}
