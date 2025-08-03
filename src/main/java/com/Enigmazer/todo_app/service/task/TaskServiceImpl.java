package com.Enigmazer.todo_app.service.task;

import com.Enigmazer.todo_app.dto.task.TaskCreationOrUpdateRequest;
import com.Enigmazer.todo_app.dto.task.TaskResponseDTO;
import com.Enigmazer.todo_app.exception.CustomExceptions.ResourceNotFoundException;
import com.Enigmazer.todo_app.mapper.TaskMapper;
import com.Enigmazer.todo_app.model.Task;
import com.Enigmazer.todo_app.model.User;
import com.Enigmazer.todo_app.repository.TaskRepository;
import com.Enigmazer.todo_app.service.JWTService;
import com.Enigmazer.todo_app.service.category.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

/**
 * {@code TaskServiceImpl} provides the business logic for managing tasks.
 * It supports creating, updating, completing, searching, and deleting tasks,
 * all scoped to the authenticated user via {@link JWTService}.
 */
@Service
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final JWTService jwtService;
    private final CategoryService categoryService;
    private final TaskMapper taskMapper;

    @Autowired
    public TaskServiceImpl(TaskRepository taskRepository, JWTService jwtService, CategoryService categoryService, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.jwtService = jwtService;
        this.categoryService = categoryService;
        this.taskMapper = taskMapper;
    }

    /**
     * Adds a new task to the database.
     *
     * @param task The task creation request
     * @return The saved task as a DTO
     */
    @Override
    public TaskResponseDTO addTask(TaskCreationOrUpdateRequest task) {
        log.info("Creating new task for user: {}", jwtService.getCurrentUser().getEmail());

        Task newTask = new Task();
        newTask.setCategory(categoryService.getCategoryById(task.getCategoryId()));
        newTask.setUser(jwtService.getCurrentUser());
        newTask.setTitle(task.getTitle());
        newTask.setDescription(task.getDescription());
        newTask.setDueDate(task.getDueDate());

        Task saved = taskRepository.save(newTask);
        log.info("Task '{}' created successfully with ID: {}", saved.getTitle(), saved.getId());
        return taskMapper.toDto(saved);
    }

    /**
     * Fetches a task by ID and ensures ownership.
     *
     * @param taskId ID of the task to fetch
     * @return The task entity
     * @throws ResourceNotFoundException if task doesn't exist or user doesn't own it
     */
    @Override
    public Task getTaskById(long taskId) {
        log.debug("Fetching task by ID: {}", taskId);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> {
                    log.warn("Task not found with ID: {}", taskId);
                    return new ResourceNotFoundException("Task not found with id: " + taskId);
                });

        User user = jwtService.getCurrentUser();
        if (!task.getUser().getId().equals(user.getId())) {
            log.warn("Unauthorized access attempt by user {} to task ID: {}", user.getEmail(), taskId);
            throw new ResourceNotFoundException("Task not found with id: " + taskId);
        }

        return task;
    }

    /**
     * Returns paginated and filtered task list for the current user.
     *
     * @param status        Completed or not
     * @param sortDirection asc or desc
     * @param sortBy        Field to sort (dueDate or lastUpdatedAt)
     * @param categoryId    Optional filter
     * @param page          Page number
     * @return Paginated list of tasks as DTOs
     */
    @Override
    public Page<TaskResponseDTO> getUserTasksSortedByDatesWithStatus(
            Boolean status,
            String sortDirection,
            String sortBy,
            Long categoryId,
            int page
    ) {
        if (page > 500) {
            log.warn("Page number too high ({}), resetting to 0", page);
            page = 0;
        }

        Long userId = jwtService.getCurrentUser().getId();

        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, 10, sort);
        log.info("Fetching tasks for user {} with filters - status: {}, categoryId: {}, page: {}, sort: {} {}",
                userId, status, categoryId, page, sortBy, sortDirection);

        if (status == null && categoryId == null) {
            return taskRepository.findAllByUserId(userId, pageable).map(taskMapper::toDto);
        } else if (status != null && categoryId == null) {
            return taskRepository.findByIsCompletedAndUserId(status, userId, pageable).map(taskMapper::toDto);
        } else if (status == null) {
            return taskRepository.findByUserIdAndCategoryId(userId, categoryId, pageable).map(taskMapper::toDto);
        } else {
            return taskRepository.findByIsCompletedAndUserIdAndCategoryId(status, userId, categoryId, pageable).map(taskMapper::toDto);
        }
    }

    /**
     * Marks a task as completed.
     *
     * @param taskId ID of the task
     * @return The updated task as DTO
     */
    @Override
    public TaskResponseDTO taskCompleted(long taskId) {
        log.info("Marking task ID: {} as completed", taskId);
        Task task = getTaskById(taskId);
        task.setCompleted(true);
        Task saved = taskRepository.save(task);
        log.info("Task ID: {} marked completed", taskId);
        return taskMapper.toDto(saved);
    }

    /**
     * Updates the task details.
     *
     * @param taskId ID of the task
     * @param task   Updated task values
     * @return The updated task as DTO
     */
    @Override
    public TaskResponseDTO updateTask(long taskId, TaskCreationOrUpdateRequest task) {
        log.info("Updating task ID: {}", taskId);
        Task existingTask = getTaskById(taskId);

        existingTask.setCategory(categoryService.getCategoryById(task.getCategoryId()));
        existingTask.setTitle(task.getTitle());

        if (task.getDescription() != null) existingTask.setDescription(task.getDescription());
        if (task.getDueDate() != null) existingTask.setDueDate(task.getDueDate());

        Task saved = taskRepository.save(existingTask);
        log.info("Task ID: {} updated successfully", taskId);
        return taskMapper.toDto(saved);
    }

    /**
     * Deletes a task after validating ownership.
     *
     * @param taskId ID of the task
     */
    @Override
    public void deleteTask(long taskId) {
        log.info("Deleting task ID: {}", taskId);
        taskRepository.delete(getTaskById(taskId));
        log.info("Task ID: {} deleted", taskId);
    }

    /**
     * Searches user's tasks by keyword in title or description.
     *
     * @param keyword Search keyword
     * @param page    Page number
     * @return Paginated results of matching tasks
     */
    @Override
    public Page<TaskResponseDTO> searchInTask(String keyword, int page) {
        if (page > 500) {
            log.warn("Page number too high ({}), resetting to 0", page);
            page = 0;
        }

        Long userId = jwtService.getCurrentUser().getId();
        log.info("Searching tasks for user {} with keyword: '{}', page: {}", userId, keyword, page);

        Pageable pageable = PageRequest.of(page, 10);
        return taskRepository.searchTask(userId, keyword, pageable).map(taskMapper::toDto);
    }
}
