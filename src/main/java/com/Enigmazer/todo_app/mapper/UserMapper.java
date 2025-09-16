package com.Enigmazer.todo_app.mapper;

import com.Enigmazer.todo_app.dto.user.UserResponseDTO;
import com.Enigmazer.todo_app.model.User;
import org.mapstruct.Mapper;

/**
 * Mapper class for mapping {@link User} object to {@link UserResponseDTO}
 */
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDTO toDto(User user);
}