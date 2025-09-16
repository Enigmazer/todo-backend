package com.Enigmazer.todo_app.repository;

import com.Enigmazer.todo_app.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link Category} entities.
 * Provides methods to perform database operations on Category entities,
 * including finding categories available to specific users and counting related tasks.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Finds all categories that are either global or belong to a specific user.
     *
     * @param userId The ID of the user
     * @return A list of categories that are either global or owned by the specified user
     */
    @Query("""
         SELECT c FROM Category c \s
         WHERE c.isGlobal = true OR c.user.id = :userId
        \s""")
    List<Category> findAvailableCategories(@Param("userId") Long userId);

    /**
     * Finds a category by its ID if it's either global or belongs to the specified user.
     *
     * @param id The ID of the category to find
     * @param userId The ID of the user making the request
     * @return An Optional containing the category if found and accessible, empty otherwise
     */
    @Query("""
         SELECT c FROM Category c \s
         WHERE c.id = :id AND (c.isGlobal = true OR c.user.id = :userId)
        \s""")
    Optional<Category> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * Finds a global category by its name.
     *
     * @param name The name of the category to find
     * @return An Optional containing the global category if found, empty otherwise
     */
    @Query("""
         SELECT c FROM Category c \s
         WHERE c.name = :name AND c.isGlobal = true
        \s""")
    Optional<Category> findByNameAndGlobal(@Param("name") String name);

    /**
     * Counts the number of tasks associated with a specific category.
     *
     * @param categoryId The ID of the category
     * @return The count of tasks in the specified category
     */
    @Query("""
         SELECT COUNT(t) FROM Task t\s
         WHERE t.category.id = :id
        \s""")
    Long countTasksByCategoryId(@Param("id") Long categoryId);

    /**
     * Counts the total number of categories that are either global or belong to a specific user.
     *
     * @param userId The ID of the user
     * @return The total count of accessible categories for the user
     */
    @Query("""
            SELECT count(c) FROM Category c \s
            WHERE c.user.id = :userId OR c.isGlobal = true""")
    Integer countCategories(@Param("userId") Long userId);
}
