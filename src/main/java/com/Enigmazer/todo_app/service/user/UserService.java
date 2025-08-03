package com.Enigmazer.todo_app.service.user;

import com.Enigmazer.todo_app.dto.token.TokenPair;
import com.Enigmazer.todo_app.dto.user.PasswordChangeRequest;
import com.Enigmazer.todo_app.dto.user.UserLoginRequest;
import com.Enigmazer.todo_app.dto.user.UserResponseDTO;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

/**
 * UserService defines the contract for user-related operations,
 * such as verifying credentials, checking password existence,
 * setting a new password, changing an existing password, and
 * retrieving current user details.
 */
public interface UserService {

    /**
     * Verifies a user's credentials and returns a JWT token if valid.
     *
     * @param users a {@link UserLoginRequest} containing email and password
     * @return a signed JWT token string if authentication succeeds
     */
    TokenPair verify(UserLoginRequest users);

    /**
     * Logs out the current user by invalidating their JWT token and clearing cache.
     *
     * @param token the JWT token to invalidate
     */
    void logout(String token);

    /**
     * Checks if the currently logged-in user's account has a password set.
     *
     * @return true if a password is set, false otherwise
     */
    boolean isPasswordAvailable();

    /**
     * Sets a new password for the currently logged-in user if no password has been set before.
     *
     * @param request a {@link UserLoginRequest} containing the email and new password
     */
    void setAPassword(@Valid UserLoginRequest request);

    /**
     * Changes the password of the currently logged-in user after verifying the old password.
     *
     * @param request a {@link PasswordChangeRequest} containing the email, old password, and new password
     */
    void changePassword(PasswordChangeRequest request);

    /**
     * Retrieves public details of the currently logged-in user.
     *
     * @return a {@link UserResponseDTO} containing user metadata
     */
    UserResponseDTO getCurrentUser();
}
