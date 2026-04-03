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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final JWTService jwtService;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
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

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getCategories() {
        Long userId = jwtService.getCurrentUser().getId();
        log.info("Fetching categories for user ID: {}", userId);

        List<Category> categories = categoryRepository.findAvailableCategories(userId);
        log.info("{} categories fetched", categories.size());

        return categories.stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Category getCategoryById(Long categoryId) {
        Long userId = jwtService.getCurrentUser().getId();
        log.info("Fetching category ID: {} for user ID: {}", categoryId, userId);

        return categoryRepository.findByIdAndUserId(categoryId, userId)
                .orElseThrow(() -> {
                    log.error("No category found with ID: {} for user ID: {}", categoryId, userId);
                    return new ResourceNotFoundException("No category found with the given ID.");
                });
    }

    @Override
    @Transactional
    public CategoryResponseDTO updateCategory(Long categoryId,CategoryCreationRequest category){
        Category existingCategory = getCategoryById(categoryId);
        if (existingCategory.isGlobal()) {
            log.debug("Attempted to updates a global category (ID: {})", categoryId);
            throw new IllegalArgumentException("Global categories can't be updated");
        }
        existingCategory.setName(category.getName());
        return categoryMapper.toDto(categoryRepository.save(existingCategory));
    }

    @Override
    @Transactional
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

    @Override
    public Integer countCategories(){
        return categoryRepository.countCategories(jwtService.getCurrentUser().getId());
    }
}
