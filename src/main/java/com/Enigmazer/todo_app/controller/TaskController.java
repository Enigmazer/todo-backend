package com.Enigmazer.todo_app.controller;

import com.Enigmazer.todo_app.dto.task.TaskCreationOrUpdateRequest;
import com.Enigmazer.todo_app.dto.task.TaskResponseDTO;
import com.Enigmazer.todo_app.service.task.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskResponseDTO> createTask(@Valid @RequestBody TaskCreationOrUpdateRequest task) {
        log.info("[TaskController] Add task request received");
        TaskResponseDTO savedTask = taskService.createTask(task);
        log.info("[TaskController] Task successfully added with ID: {}", savedTask.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTask);
    }

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

    @PutMapping("/{taskId}")
    public ResponseEntity<TaskResponseDTO> updateTask(
            @PathVariable long taskId,
            @Valid @RequestBody TaskCreationOrUpdateRequest task) {

        log.info("[TaskController] Update request for taskId={}", taskId);
        TaskResponseDTO updated = taskService.updateTask(taskId, task);
        log.info("[TaskController] Task updated successfully (taskId={})", taskId);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{taskId}/toggle-completion")
    public ResponseEntity<TaskResponseDTO> toggleTaskCompletion(@PathVariable long taskId) {
        log.info("changing task status (taskId={})", taskId);
        TaskResponseDTO completed = taskService.toggleTaskCompletion(taskId);
        log.info("task status changes (taskId={})", taskId);
        return ResponseEntity.ok(completed);
    }

    @PutMapping("/{taskId}/toggle-notification")
    public ResponseEntity<TaskResponseDTO> toggleTaskNotification(@PathVariable long taskId) {
        log.info("changing send email state (taskId={})", taskId);
        TaskResponseDTO completed = taskService.toggleTaskNotification(taskId);
        log.info("send email status changed (taskId={})", taskId);
        return ResponseEntity.ok(completed);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteTasks(@RequestBody List<Long> taskIds) {
        taskService.deleteTasks(taskIds);
        log.info("Task deleted (taskId={})", taskIds);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<TaskResponseDTO>> searchTasks(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page) {

        log.info("[TaskController] Search request received (keyword='{}', page={})", keyword, page);
        Page<TaskResponseDTO> results = taskService.searchTasks(keyword, page);
        return wrapPageResponse(results);
    }

    @GetMapping("/count")
    public ResponseEntity<Integer> countTasks(){
        return ResponseEntity.ok(taskService.countTasks());
    }

    @GetMapping("/count-completed")
    public ResponseEntity<Integer> countCompletedTasks(){
        return ResponseEntity.ok(taskService.countCompletedTasks());
    }

    @GetMapping("/count/{categoryId}")
    public ResponseEntity<Integer> countTasksInCategory(@PathVariable Long categoryId){
        return ResponseEntity.ok(taskService.countTasksInCategory(categoryId));
    }

    public ResponseEntity<Page<TaskResponseDTO>> wrapPageResponse(Page<TaskResponseDTO> taskPage) {
        if (taskPage.isEmpty()) {
            log.info("[TaskController] No content found for query");
            return ResponseEntity.noContent().build();
        }
        log.info("[TaskController] Returning {} tasks", taskPage.getNumberOfElements());
        return ResponseEntity.ok(taskPage);
    }
}
