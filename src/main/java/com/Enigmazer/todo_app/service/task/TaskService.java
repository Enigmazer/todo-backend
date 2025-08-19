package com.Enigmazer.todo_app.service.task;

import com.Enigmazer.todo_app.dto.task.TaskCreationOrUpdateRequest;
import com.Enigmazer.todo_app.dto.task.TaskResponseDTO;
import com.Enigmazer.todo_app.model.Task;
import org.springframework.data.domain.Page;

import java.util.List;

public interface TaskService {

    TaskResponseDTO addTask(TaskCreationOrUpdateRequest task);

    Task getTaskById(long taskId) ;

    Page<TaskResponseDTO> getUserTasksSortedByDatesWithStatus(Boolean status, String sortDirection, String sortBy, Long categoryId, int page);

    TaskResponseDTO taskCompleted(long taskId) ;

    TaskResponseDTO updateTask(long taskId, TaskCreationOrUpdateRequest task) ;

    void deleteTask(List<Long> taskIds) ;

    Page<TaskResponseDTO> searchInTask(String keyword, int page);

    Integer totalTasksOfUser();

    Integer totalCompletedTasksOfUser();

    Integer totalTasksOfUserInCategory(Long categoryId);
}
