package com.Enigmazer.todo_app.mapper;

import com.Enigmazer.todo_app.dto.category.CategoryResponseDTO;
import com.Enigmazer.todo_app.model.Category;
import org.mapstruct.Mapper;

/**
 * Mapper class for mapping {@link Category} object to {@link CategoryResponseDTO}
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {
    CategoryResponseDTO toDto(Category category);
}
