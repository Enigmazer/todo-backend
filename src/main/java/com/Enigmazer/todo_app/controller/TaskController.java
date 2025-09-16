package com.Enigmazer.todo_app.controller;

import com.Enigmazer.todo_app.dto.task.TaskCreationOrUpdateRequest;
import com.Enigmazer.todo_app.dto.task.TaskResponseDTO;
import com.Enigmazer.todo_app.service.task.TaskService;
import com.Enigmazer.todo_app.service.task.TaskServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
 *     <li>Change task completed status</li>
 *     <li>Change task email enable status</li>
 *     <li>Delete task</li>
 *     <li>Search tasks by keyword</li>
 *     <li>Get total tasks count</li>
 *     <li>Get total completed tasks count</li>
 *     <li>Get total tasks count in a category</li>
 * </ul>
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    /**
     * Adds a new task to the database.
     *
     * @param task Task creation data
     * @return The created task
     */
    @PostMapping
    public ResponseEntity<TaskResponseDTO> createTask(@Valid @RequestBody TaskCreationOrUpdateRequest task) {
        log.info("[TaskController] Add task request received");
        TaskResponseDTO savedTask = taskService.createTask(task);
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
                .getTasks(status, sortDirection, sortBy, categoryId, page);

        return wrapPageResponse(tasks);
    }

    /**
     * Updates a task by ID.
     *
     * @param taskId ID of the task
     * @param task   Updated data
     * @return The updated task
     */
    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponseDTO> updateTask(
            @PathVariable long taskId,
            @Valid @RequestBody TaskCreationOrUpdateRequest task) {

        log.info("[TaskController] Update request for taskId={}", taskId);
        TaskResponseDTO updated = taskService.updateTask(taskId, task);
        log.info("[TaskController] Task updated successfully (taskId={})", taskId);
        return ResponseEntity.ok(updated);
    }

    /**
     * Change task completion status.
     *
     * @param taskId ID of the task
     * @return The updated task
     */
    @PutMapping("/{taskId}/toggle-completion")
    public ResponseEntity<TaskResponseDTO> toggleTaskCompletion(@PathVariable long taskId) {
        log.info("changing task status (taskId={})", taskId);
        TaskResponseDTO completed = taskService.toggleTaskCompletion(taskId);
        log.info("task status changes (taskId={})", taskId);
        return ResponseEntity.ok(completed);
    }

    /**
     * Enable or disable the email notification for the task
     *
     * @param taskId ID of the task
     * @return updated task
     */
    @PutMapping("/{taskId}/toggle-notification")
    public ResponseEntity<TaskResponseDTO> toggleTaskNotification(@PathVariable long taskId) {
        log.info("changing send email state (taskId={})", taskId);
        TaskResponseDTO completed = taskService.toggleTaskNotification(taskId);
        log.info("send email status changed (taskId={})", taskId);
        return ResponseEntity.ok(completed);
    }

    /**
     * Deletes a list of tasks by ID.
     *
     * @param taskIds List of task ids
     * @return HTTP 204 if successful
     */
    @DeleteMapping
    public ResponseEntity<Void> deleteTasks(@RequestBody List<Long> taskIds) {
        taskService.deleteTasks(taskIds);
        log.info("Task deleted (taskId={})", taskIds);
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
    public ResponseEntity<Page<TaskResponseDTO>> searchTasks(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page) {

        log.info("[TaskController] Search request received (keyword='{}', page={})", keyword, page);
        Page<TaskResponseDTO> results = taskService.searchTasks(keyword, page);
        return wrapPageResponse(results);
    }

    /**
     * return total task count of a user
     *
     * @return total tasks count
     */
    @GetMapping("/count")
    public ResponseEntity<Integer> countTasks(){
        return ResponseEntity.ok(taskService.countTasks());
    }

    /**
     * return total completed task count of a user
     *
     * @return total completed tasks count
     */
    @GetMapping("/count-completed")
    public ResponseEntity<Integer> countCompletedTasks(){
        return ResponseEntity.ok(taskService.countCompletedTasks());
    }

    /**
     * return total task count of a user in
     * a category
     *
     * @return total tasks count in a category
     */
    @GetMapping("/count/{categoryId}")
    public ResponseEntity<Integer> countTasksInCategory(@PathVariable Long categoryId){
        return ResponseEntity.ok(taskService.countTasksInCategory(categoryId));
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
