package com.Enigmazer.todo_app.repository;

import com.Enigmazer.todo_app.model.RefreshToken;
import com.Enigmazer.todo_app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repository interface for {@link RefreshToken} entities.
 * Provides methods to perform database operations on refresh tokens,
 * including finding, deleting, and managing token-related user information.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    /**
     * Finds a refresh token by its token string.
     *
     * @param token The token string to search for
     * @return An Optional containing the refresh token if found, empty otherwise
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Finds the user associated with a specific refresh token.
     *
     * @param token The refresh token string
     * @return An Optional containing the user if the token exists, empty otherwise
     */
    @Query("SELECT r.user FROM RefreshToken r WHERE r.token = :token")
    Optional<User> findUserByToken(@Param("token") String token);

    /**
     * Finds the most recent refresh token for a specific user.
     * Tokens are ordered by creation date in descending order.
     *
     * @param userId The ID of the user
     * @return An Optional containing the most recent refresh token for the user, if any
     */
    Optional<RefreshToken> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Deletes a refresh token by its token string.
     *
     * @param token The token string to delete
     */
    void deleteByToken(String token);

    /**
     * Deletes all refresh tokens associated with a specific user.
     * This is typically used when a user logs out.
     *
     * @param user The user whose tokens should be deleted
     */
    void deleteByUser(User user);
}