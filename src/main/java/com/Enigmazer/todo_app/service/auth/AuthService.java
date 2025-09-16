package com.Enigmazer.todo_app.service.auth;

import com.Enigmazer.todo_app.dto.token.TokenPair;
import com.Enigmazer.todo_app.dto.user.UserLoginRequest;

/**
 * AuthService defines the contract for authentication related operations,
 * such as verifying credentials and changing an existing password.
 */
public interface AuthService {

    /**
     * Verifies a user's credentials and returns a {@link TokenPair} if valid.
     *
     * @param users a {@link UserLoginRequest} containing email and password
     * @return a Token pair (containing access and refresh token) if authentication succeeds
     */
    TokenPair login(UserLoginRequest users);

    /**
     * Logs out the current user by invalidating their JWT token and clearing cache.
     *
     * @param token the JWT token to invalidate
     */
    void logout(String token);

    /**
     * Changes the password of the currently logged-in user.
     *
     * @param request a {@link UserLoginRequest} containing the email and password
     */
    void changePassword(UserLoginRequest request);

    /**
     * Retrieves the latest refresh token for the current user
     * @return refresh token string
     */
    String getLatestRefreshTokenForUser();
}
