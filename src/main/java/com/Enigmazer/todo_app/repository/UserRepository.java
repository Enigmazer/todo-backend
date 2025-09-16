package com.Enigmazer.todo_app.repository;

import com.Enigmazer.todo_app.model.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for {@link User} entities.
 * Provides methods to perform database operations on users,
 * including authentication and OAuth-related queries.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their unique identifier.
     *
     * @param id The ID of the user to find
     * @return An Optional containing the user if found, empty otherwise
     */
    Optional<User> findById(long id);

    /**
     * Finds a user by their email address.
     * Email is guaranteed to be unique across all users.
     *
     * @param email The email address to search for
     * @return An Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds a user by their OAuth provider and provider-specific ID.
     * This is used for OAuth authentication to find existing users.
     *
     * @param provider The OAuth provider (e.g., "google", "github")
     * @param providerId The user's ID from the OAuth provider
     * @return An Optional containing the user if found, empty otherwise
     */
    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    /**
     * Checks if a user with the given email already exists in the system.
     *
     * @param email The email address to check
     * @return true if a user with the email exists, false otherwise
     * @throws IllegalArgumentException if the email is null
     */
    boolean existsByEmail(@NotNull String email);

}
