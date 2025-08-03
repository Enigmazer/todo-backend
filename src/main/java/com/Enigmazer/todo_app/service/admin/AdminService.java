package com.Enigmazer.todo_app.service.admin;

import com.Enigmazer.todo_app.dto.category.CategoryCreationRequest;
import com.Enigmazer.todo_app.dto.category.CategoryResponseDTO;
import com.Enigmazer.todo_app.dto.user.UserLoginRequest;
import com.Enigmazer.todo_app.dto.user.UserRegistrationRequest;
import com.Enigmazer.todo_app.model.User;

import java.util.List;

/**
 * AdminService defines the contract for admin-related operations,
 * such as register new admin user, enable or disable a users account,
 * delete a user, get a list of all users, add a global category
 */
public interface AdminService {
    /**
     * Registers a new admin user with the given email and password.
     *
     * @param user DTO containing email and password
     */
    void registerUser(UserRegistrationRequest user);

    /**
     * Changes the status (enabled/disabled) of a user account.
     *
     * @param userId ID of the user to modify
     * @param status true to enable, false to disable
     */
    void changeAccountStatus(long userId, boolean status);

    /**
     * Deletes a user from the database by ID.
     *
     * @param userId ID of the user to delete
     */
    void deleteUser(long userId);

    /**
     * Returns all users from the database.
     *
     * @return list of all user objects
     */
    List<User> getAllUsers();

    /**
     * Adds a new global category to the system. Global categories
     * are shared across all users and cannot be deleted by normal users.
     *
     * @param category DTO containing category data
     * @return the created category response
     */
    CategoryResponseDTO addGlobalCategory(CategoryCreationRequest category);

}
