package com.Enigmazer.todo_app.service.category;

import com.Enigmazer.todo_app.dto.category.CategoryResponseDTO;
import com.Enigmazer.todo_app.dto.category.CategoryCreationRequest;
import com.Enigmazer.todo_app.exception.CustomExceptions.ResourceNotFoundException;
import com.Enigmazer.todo_app.mapper.CategoryMapper;
import com.Enigmazer.todo_app.model.Category;
import com.Enigmazer.todo_app.repository.CategoryRepository;
import com.Enigmazer.todo_app.service.JWTService;
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
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final JWTService jwtService;
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository, JWTService jwtService, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.jwtService = jwtService;
        this.categoryMapper = categoryMapper;
    }

    /**
     * Adds a new category for the currently authenticated user.
     *
     * @param category The category creation request containing the name
     * @return DTO of the saved category
     */
    @Override
    public CategoryResponseDTO addCategory(CategoryCreationRequest category) {
        log.info("[CategoryService] Attempting to add category: {}", category.getName());

        Category newCategory = new Category();
        newCategory.setName(category.getName());
        newCategory.setUser(jwtService.getCurrentUser());

        Category saved = categoryRepository.save(newCategory);
        log.info("[CategoryService] Category saved successfully with ID: {}", saved.getId());

        return categoryMapper.toDto(saved);
    }

    /**
     * Fetches all available categories for the current user.
     *
     * @return List of user-specific categories
     */
    @Override
    public List<CategoryResponseDTO> getCategories() {
        Long userId = jwtService.getCurrentUser().getId();
        log.info("[CategoryService] Fetching categories for user ID: {}", userId);

        List<Category> categories = categoryRepository.findAvailableCategories(userId);
        log.info("[CategoryService] {} categories fetched", categories.size());

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
        log.info("[CategoryService] Fetching category ID: {} for user ID: {}", categoryId, userId);

        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> {
                    log.error("[CategoryService] No category found with ID: {} for user ID: {}", categoryId, userId);
                    return new ResourceNotFoundException("No category found with the given ID.");
                });
    }

    /**
     * Deletes a category by ID if it's not marked as global.
     *
     * @param categoryId ID of the category to delete
     */
    @Override
    public void deleteCategory(Long categoryId) {
        log.info("[CategoryService] Deletion request received for category ID: {}", categoryId);
        Category category = getCategoryById(categoryId);

        if (category.isGlobal()) {
            log.error("[CategoryService] Attempted to delete a global category (ID: {})", categoryId);
            throw new IllegalArgumentException("Global categories can't be deleted");
        }

        categoryRepository.delete(category);
        log.info("[CategoryService] Category deleted successfully (ID: {})", categoryId);
    }
}
