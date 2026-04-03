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

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findAllByUserId(Long userId, Pageable pageable);

    Page<Task> findByIsCompletedAndUserId(boolean isCompleted, Long userId, Pageable pageable);

    Page<Task> findByUserIdAndCategoryId(Long userId, Long categoryId, Pageable pageable);

    Page<Task> findByIsCompletedAndUserIdAndCategoryId(boolean isCompleted, Long userId, Long categoryId, Pageable pageable);

    @Query("SELECT t FROM Task t WHERE " +
            "t.user.id = :userId AND (" +
            "LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(COALESCE(t.description, '')) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Task> searchTasks(@Param("userId") Long userId, @Param("keyword") String keyword, Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM Task t WHERE t.id IN :taskIds AND t.user.id = :userId")
    void deleteTasks(@Param("taskIds") List<Long> taskIds, @Param("userId") Long userId);

    @Query("SELECT count(t) FROM Task t WHERE t.user.id = :userId")
    Integer countTasks(@Param("userId") Long userId);

    @Query("SELECT count(t) FROM Task t WHERE t.user.id = :userId AND t.isCompleted = true")
    Integer countCompletedTasks(@Param("userId") Long userId);

    @Query("SELECT count(t) FROM Task t WHERE t.user.id = :userId AND t.category.id = :categoryId")
    Integer countTasksInCategory(@Param("userId") Long userId, @Param("categoryId") Long categoryId);

    @Query("SELECT t FROM Task t JOIN FETCH t.user WHERE " +
            "t.isEmailEnabled = true AND t.isReminderSent = " +
            "false AND t.dueDate BETWEEN :start AND :end ")
    List<Task> findRemindableTasks(@Param("start") Instant start, @Param("end") Instant end);
}
