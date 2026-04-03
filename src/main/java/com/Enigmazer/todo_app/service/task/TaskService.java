package com.Enigmazer.todo_app.service.task;

import com.Enigmazer.todo_app.dto.task.TaskCreationOrUpdateRequest;
import com.Enigmazer.todo_app.dto.task.TaskResponseDTO;
import com.Enigmazer.todo_app.model.Task;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TaskService {

    TaskResponseDTO createTask(TaskCreationOrUpdateRequest task);

    Task getTaskById(long taskId);

    Page<TaskResponseDTO> getTasks(Boolean status, String sortDirection, String sortBy, Long categoryId, int page);

    Page<TaskResponseDTO> searchTasks(String keyword, int page);

    TaskResponseDTO toggleTaskCompletion(long taskId);

    TaskResponseDTO toggleTaskNotification(long taskId);

    TaskResponseDTO updateTask(long taskId, TaskCreationOrUpdateRequest task);

    void deleteTasks(List<Long> taskIds);

    Integer countTasks();

    Integer countCompletedTasks();

    Integer countTasksInCategory(Long categoryId);
}
