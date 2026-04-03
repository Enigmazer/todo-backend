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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final JWTService jwtService;
    private final CategoryService categoryService;
    private final TaskMapper taskMapper;

    @Override
    @Transactional
    public TaskResponseDTO createTask(TaskCreationOrUpdateRequest task) {
        log.info("Creating new task for user: {}", jwtService.getCurrentUser().getEmail());

        Task saved = taskRepository.save(
                Task.builder()
                        .category(categoryService.getCategoryById(task.getCategoryId()))
                        .user(jwtService.getCurrentUser())
                        .title(task.getTitle())
                        .description(task.getDescription())
                        .dueDate(task.getDueDate())
                        .build()
        );

        log.info("Task '{}' created successfully with ID: {}", saved.getTitle(), saved.getId());

        return taskMapper.toDto(saved);
    }

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

    @Override
    public Page<TaskResponseDTO> getTasks(
            Boolean status,
            String sortDirection,
            String sortBy,
            Long categoryId,
            int page
    ) {
        page = getTaskPage(page);

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

    @Override
    public Page<TaskResponseDTO> searchTasks(String keyword, int page) {
        page = getTaskPage(page);
        Long userId = jwtService.getCurrentUser().getId();
        log.info("Searching tasks for user {} with keyword: '{}', page: {}", userId, keyword, page);

        Pageable pageable = PageRequest.of(page, 10);
        return taskRepository.searchTasks(userId, keyword, pageable).map(taskMapper::toDto);
    }

    private int getTaskPage(int page){
        if (page > 500) {
            log.warn("Page number too high ({}), resetting to 0", page);
            return 0;
        }
        return page;
    }

    @Override
    @Transactional
    public TaskResponseDTO toggleTaskCompletion(long taskId) {
        log.info("changing task ID: {} status", taskId);
        Task task = getTaskById(taskId);
        task.setCompleted(!task.isCompleted());
        task.setReminderSent(task.isCompleted());
        task = taskRepository.save(task);
        log.info("Task ID: {} status changed", taskId);
        return taskMapper.toDto(task);
    }

    @Override
    @Transactional
    public TaskResponseDTO toggleTaskNotification(long taskId) {
        log.info("changing send email state (taskId={})", taskId);
        Task task = getTaskById(taskId);
        task.setEmailEnabled(!task.isEmailEnabled());
        task = taskRepository.save(task);
        log.info("send email status changed (taskId={})", taskId);
        return taskMapper.toDto(task);
    }

    @Override
    @Transactional
    public TaskResponseDTO updateTask(long taskId, TaskCreationOrUpdateRequest task) {
        log.info("Updating task ID: {}", taskId);

        Task existingTask = getTaskById(taskId);
        existingTask.setCategory(categoryService.getCategoryById(task.getCategoryId()));
        existingTask.setTitle(task.getTitle());
        if (task.getDueDate().isAfter(existingTask.getDueDate())) {
            existingTask.setDueDate(task.getDueDate());
            existingTask.setReminderSent(false);
        }else {
            existingTask.setDueDate(task.getDueDate());
        }

        if (task.getDescription() != null && task.getDescription().trim().isBlank()) {
            existingTask.setDescription(task.getDescription());
        }

        log.info("Task ID: {} updated successfully", taskId);
        return taskMapper.toDto(taskRepository.save(existingTask));
    }

    @Override
    @Transactional
    public void deleteTasks(List<Long> taskIds) {
        log.info("Deleting task ID: {}", taskIds);

        User user = jwtService.getCurrentUser();
        taskRepository.deleteTasks(taskIds, user.getId());

        log.info("Task ID: {} deleted", taskIds);
    }

    @Override
    public Integer countTasks(){
        return taskRepository.countTasks(jwtService.getCurrentUser().getId());
    }

    @Override
    public Integer countCompletedTasks(){
        return taskRepository.countCompletedTasks(jwtService.getCurrentUser().getId());
    }

    @Override
    public Integer countTasksInCategory(Long categoryId){
        return taskRepository.countTasksInCategory(jwtService.getCurrentUser().getId(), categoryId);
    }
}
