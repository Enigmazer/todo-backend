package com.Enigmazer.todo_app.service.category;

import com.Enigmazer.todo_app.dto.category.CategoryCreationRequest;
import com.Enigmazer.todo_app.dto.category.CategoryResponseDTO;
import com.Enigmazer.todo_app.model.Category;

import java.util.List;

public interface CategoryService {

    CategoryResponseDTO createCategory(CategoryCreationRequest category);

    List<CategoryResponseDTO> getCategories();

    Category getCategoryById(Long categoryId);

    void deleteCategory(Long categoryId);

    Integer countCategories();

    CategoryResponseDTO updateCategory(Long categoryId, CategoryCreationRequest category);
}