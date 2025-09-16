package com.Enigmazer.todo_app.service.task;

import com.Enigmazer.todo_app.dto.task.TaskCreationOrUpdateRequest;
import com.Enigmazer.todo_app.dto.task.TaskResponseDTO;
import com.Enigmazer.todo_app.model.Task;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Service interface for managing tasks in the todo application.
 * Provides operations for creating, reading, updating, and deleting tasks,
 * as well as retrieving task statistics and search functionality.
 */
public interface TaskService {

    /**
     * Creates a new task with the provided details.
     *
     * @param task The task creation request containing the task details
     * @return A DTO containing the created task's information
     * @throws com.Enigmazer.todo_app.exception.CustomExceptions.ResourceNotFoundException if the associated category is not found
     */
    TaskResponseDTO createTask(TaskCreationOrUpdateRequest task);

    /**
     * Retrieves a task by its ID if it belongs to the current user.
     *
     * @param taskId The ID of the task to retrieve
     * @return The task entity if found and accessible
     * @throws com.Enigmazer.todo_app.exception.CustomExceptions.ResourceNotFoundException if the task is not found or not accessible
     */
    Task getTaskById(long taskId);

    /**
     * Retrieves a paginated list of tasks for the current user, optionally filtered by status and category.
     * Results can be sorted by different fields and directions.
     *
     * @param status Optional filter for task completion status
     * @param sortDirection The direction to sort the results (e.g., "asc" or "desc")
     * @param sortBy The field to sort by (e.g., "dueDate", "createdAt")
     * @param categoryId Optional category ID to filter tasks
     * @param page The page number for pagination (0-based)
     * @return A page of task DTOs matching the criteria
     */
    Page<TaskResponseDTO> getTasks(Boolean status, String sortDirection, String sortBy, Long categoryId, int page);

    /**
     * Searches for tasks containing the given keyword in their title or description.
     *
     * @param keyword The search term to look for (case-insensitive)
     * @param page The page number for pagination (0-based)
     * @return A page of task DTOs matching the search criteria
     */
    Page<TaskResponseDTO> searchTasks(String keyword, int page);

    /**
     * Toggles the completion status of a task.
     *
     * @param taskId The ID of the task to update
     * @return A DTO containing the updated task's information
     * @throws com.Enigmazer.todo_app.exception.CustomExceptions.ResourceNotFoundException if the task is not found or is not owned by the current user
     */
    TaskResponseDTO toggleTaskCompletion(long taskId);

    /**
     * Toggles the email notification setting for a task.
     *
     * @param taskId The ID of the task to update
     * @return A DTO containing the updated task's information
     * @throws com.Enigmazer.todo_app.exception.CustomExceptions.ResourceNotFoundException if the task is not found or is not owned by the current user
     */
    TaskResponseDTO toggleTaskNotification(long taskId);

    /**
     * Updates an existing task with new details.
     *
     * @param taskId The ID of the task to update
     * @param task The updated task details
     * @return A DTO containing the updated task's information
     * @throws com.Enigmazer.todo_app.exception.CustomExceptions.ResourceNotFoundException if the task is not found or is not owned by the current user
     */
    TaskResponseDTO updateTask(long taskId, TaskCreationOrUpdateRequest task);

    /**
     * Deletes multiple tasks by their IDs if they belong to the current user.
     *
     * @param taskIds The list of task IDs to delete
     */
    void deleteTasks(List<Long> taskIds);

    /**
     * Counts the total number of tasks for the current user.
     *
     * @return The total count of tasks
     */
    Integer countTasks();

    /**
     * Counts the number of completed tasks for the current user.
     *
     * @return The count of completed tasks
     */
    Integer countCompletedTasks();

    /**
     * Counts the number of tasks in a specific category for the current user.
     *
     * @param categoryId The ID of the category
     * @return The count of tasks in the specified category
     */
    Integer countTasksInCategory(Long categoryId);

}
