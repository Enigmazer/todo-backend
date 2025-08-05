package com.Enigmazer.todo_app.controller;

import com.Enigmazer.todo_app.dto.task.TaskCreationOrUpdateRequest;
import com.Enigmazer.todo_app.dto.task.TaskResponseDTO;
import com.Enigmazer.todo_app.service.task.TaskService;
import com.Enigmazer.todo_app.service.task.TaskServiceImpl;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * TaskController handles all task-related endpoints.
 * <p>
 * All endpoints are secured using JWT and managed by {@link TaskServiceImpl}.
 * <p>
 * Supported operations:
 * <ul>
 *     <li>Add a new task</li>
 *     <li>Fetch user tasks with filters</li>
 *     <li>Update an existing task</li>
 *     <li>Mark task as completed</li>
 *     <li>Delete task</li>
 *     <li>Search tasks by keyword</li>
 * </ul>
 */
@RestController
@RequestMapping("/task")
@Slf4j
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Adds a new task to the database.
     *
     * @param task Task creation data
     * @return The created task
     */
    @PostMapping
    public ResponseEntity<TaskResponseDTO> addTask(@Valid @RequestBody TaskCreationOrUpdateRequest task) {
        log.info("[TaskController] Add task request received");
        TaskResponseDTO savedTask = taskService.addTask(task);
        log.info("[TaskController] Task successfully added with ID: {}", savedTask.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTask);
    }

    /**
     * Retrieves all user tasks, with optional filtering by status and category,
     * sorting by field and direction, and paginated.
     *
     * @param status        Optional - completed (true) or not completed (false)
     * @param categoryId    Optional - category ID to filter
     * @param sortDirection asc or desc (default is asc)
     * @param sortBy        field to sort by (default is lastUpdatedAt)
     * @param page          page number for pagination (default is 0)
     * @return A page of tasks
     */
    @GetMapping
    public ResponseEntity<Page<TaskResponseDTO>> getTasks(
            @RequestParam(required = false) Boolean status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestParam(defaultValue = "lastUpdatedAt") String sortBy,
            @RequestParam(defaultValue = "0") int page
    ) {
        log.info("[TaskController] Fetching tasks (status={}, categoryId={}, sortBy={}, sortDir={}, page={})",
                status, categoryId, sortBy, sortDirection, page);

        Page<TaskResponseDTO> tasks = taskService
                .getUserTasksSortedByDatesWithStatus(status, sortDirection, sortBy, categoryId, page);

        return wrapPageResponse(tasks);
    }

    /**
     * Updates a task by ID.
     *
     * @param taskId Task ID to update
     * @param task   Updated data
     * @return The updated task
     */
    @PutMapping("/update/{taskId}")
    public ResponseEntity<TaskResponseDTO> updateTask(
            @PathVariable long taskId,
            @Valid @RequestBody TaskCreationOrUpdateRequest task) {

        log.info("[TaskController] Update request for taskId={}", taskId);
        TaskResponseDTO updated = taskService.updateTask(taskId, task);
        log.info("[TaskController] Task updated successfully (taskId={})", taskId);
        return ResponseEntity.ok(updated);
    }

    /**
     * Marks a task as completed.
     *
     * @param taskId ID of the task
     * @return The completed task
     */
    @PutMapping("/completed/{taskId}")
    public ResponseEntity<TaskResponseDTO> taskCompleted(@PathVariable long taskId) {
        log.info("[TaskController] Marking task as completed (taskId={})", taskId);
        TaskResponseDTO completed = taskService.taskCompleted(taskId);
        log.info("[TaskController] Task marked as completed (taskId={})", taskId);
        return ResponseEntity.ok(completed);
    }

    /**
     * Deletes a task by ID.
     *
     * @param taskId Task ID to delete
     * @return HTTP 204 if successful
     */
    @DeleteMapping("/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable long taskId) {
        log.info("[TaskController] Delete request for taskId={}", taskId);
        taskService.deleteTask(taskId);
        log.info("[TaskController] Task deleted (taskId={})", taskId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Searches tasks using a keyword.
     *
     * @param keyword Search keyword
     * @param page    Page number
     * @return Page of matching tasks
     */
    @GetMapping("/search")
    public ResponseEntity<Page<TaskResponseDTO>> searchInTasks(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page) {

        log.info("[TaskController] Search request received (keyword='{}', page={})", keyword, page);
        Page<TaskResponseDTO> results = taskService.searchInTask(keyword, page);
        return wrapPageResponse(results);
    }

    /**
     * Helper method to wrap paginated responses.
     *
     * @param taskPage Page of tasks
     * @return HTTP 204 if empty, else HTTP 200 with content
     */
    public ResponseEntity<Page<TaskResponseDTO>> wrapPageResponse(Page<TaskResponseDTO> taskPage) {
        if (taskPage.isEmpty()) {
            log.info("[TaskController] No content found for query");
            return ResponseEntity.noContent().build();
        }
        log.info("[TaskController] Returning {} tasks", taskPage.getNumberOfElements());
        return ResponseEntity.ok(taskPage);
    }
}
