package com.Enigmazer.todo_app.service.user;

import com.Enigmazer.todo_app.dto.user.UserResponseDTO;

/**
 * UserService defines the contract for user-related operations.
 */
public interface UserService {

    /**
     * Retrieves public details of the currently logged-in user.
     *
     * @return a {@link UserResponseDTO} containing user metadata
     */
    UserResponseDTO getCurrentUser();
}
