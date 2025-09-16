package com.Enigmazer.todo_app.repository;

import com.Enigmazer.todo_app.model.Task;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository interface for {@link Task} entities.
 * Provides methods to perform database operations on tasks,
 * including pagination, filtering, and search functionality.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Retrieves a page of tasks for a specific user with pagination support.
     *
     * @param userId The ID of the user
     * @param pageable Pagination information
     * @return A page of tasks belonging to the specified user
     */

    Page<Task> findAllByUserId(Long userId, Pageable pageable);

    /**
     * Retrieves a page of tasks filtered by completion status for a specific user.
     *
     * @param isCompleted The completion status to filter by
     * @param userId The ID of the user
     * @param pageable Pagination information
     * @return A page of tasks matching the completion status and user
     */

    Page<Task> findByIsCompletedAndUserId(boolean isCompleted, Long userId, Pageable pageable);

    /**
     * Retrieves a page of tasks for a specific user and category.
     *
     * @param userId The ID of the user
     * @param categoryId The ID of the category
     * @param pageable Pagination information
     * @return A page of tasks belonging to the specified user and category
     */

    Page<Task> findByUserIdAndCategoryId(Long userId, Long categoryId, Pageable pageable);

    /**
     * Retrieves a page of tasks filtered by completion status, user, and category.
     *
     * @param isCompleted The completion status to filter by
     * @param userId The ID of the user
     * @param categoryId The ID of the category
     * @param pageable Pagination information
     * @return A page of tasks matching all specified criteria
     */

    Page<Task> findByIsCompletedAndUserIdAndCategoryId(boolean isCompleted, Long userId, Long categoryId, Pageable pageable);

    /**
     * Searches tasks by keyword in the title or description for the specified user.
     *
     * @param userId  ID of the user whose tasks are to be searched.
     * @param keyword The keyword to search for (case-insensitive, partial match).
     * @param pageable Pagination details (page number, size, sort).
     * @return A page of tasks matching the keyword in title or description.
     */
    @Query("SELECT t FROM Task t WHERE " +
            "t.user.id = :userId AND (" +
            "LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(COALESCE(t.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Task> searchTasks(@Param("userId") Long userId, @Param("keyword") String keyword, Pageable pageable);

    /**
     * Deletes multiple tasks by their IDs if they belong to the specified user.
     * This operation is performed in a single database call for efficiency.
     *
     * @param taskIds List of task IDs to delete
     * @param userId The ID of the user who owns the tasks
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM Task t WHERE t.id IN :taskIds AND t.user.id = :userId")
    void deleteTasks(@Param("taskIds") List<Long> taskIds, @Param("userId") Long userId);

    /**
     * Counts the total number of tasks for a specific user.
     *
     * @param userId The ID of the user
     * @return The count of tasks belonging to the user
     */
    @Query("SELECT count(t) FROM Task t WHERE t.user.id = :userId")
    Integer countTasks(@Param("userId") Long userId);

    /**
     * Counts the number of completed tasks for a specific user.
     *
     * @param userId The ID of the user
     * @return The count of completed tasks belonging to the user
     */
    @Query("SELECT count(t) FROM Task t WHERE t.user.id = :userId AND t.isCompleted = true")
    Integer countCompletedTasks(@Param("userId") Long userId);

    /**
     * Counts the number of tasks for a specific user in a specific category.
     *
     * @param userId The ID of the user
     * @param categoryId The ID of the category
     * @return The count of tasks in the specified category for the user
     */
    @Query("SELECT count(t) FROM Task t WHERE t.user.id = :userId AND t.category.id = :categoryId")
    Integer countTasksInCategory(@Param("userId") Long userId, @Param("categoryId") Long categoryId);

    /**
     * Retrieves all tasks with due dates within the specified range, including user information.
     * Uses JOIN FETCH to optimize the query by loading user data in the same database call.
     *
     * @param start The start of the date range (inclusive)
     * @param end The end of the date range (inclusive)
     * @return A list of tasks with due dates in the specified range
     */
    @Query("SELECT t FROM Task t JOIN FETCH t.user WHERE " +
            "t.isEmailEnabled = true AND t.isReminderSent = " +
            "false AND t.dueDate BETWEEN :start AND :end ")
    List<Task> findRemindableTasks(@Param("start") Instant start, @Param("end") Instant end);
}
