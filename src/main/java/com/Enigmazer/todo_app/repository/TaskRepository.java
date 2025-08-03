package com.Enigmazer.todo_app.repository;

import com.Enigmazer.todo_app.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findAllByUserId(Long userId, Pageable pageable);

    Page<Task> findByIsCompletedAndUserId(boolean isCompleted, Long userId, Pageable pageable);

    Page<Task> findByUserIdAndCategoryId(Long userId, Long categoryId, Pageable pageable);

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
    Page<Task> searchTask(@Param("userId") Long userId, @Param("keyword") String keyword, Pageable pageable);

}
