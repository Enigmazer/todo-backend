package com.Enigmazer.todo_app.service.task;

import com.Enigmazer.todo_app.dto.task.TaskCreationOrUpdateRequest;
import com.Enigmazer.todo_app.dto.task.TaskResponseDTO;
import com.Enigmazer.todo_app.exception.CustomExceptions.ResourceNotFoundException;
import com.Enigmazer.todo_app.mapper.TaskMapper;
import com.Enigmazer.todo_app.model.Category;
import com.Enigmazer.todo_app.model.Task;
import com.Enigmazer.todo_app.model.User;
import com.Enigmazer.todo_app.repository.TaskRepository;
import com.Enigmazer.todo_app.service.JWTService;
import com.Enigmazer.todo_app.service.category.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock private TaskRepository taskRepository;
    @Mock private JWTService jwtService;
    @Mock private CategoryService categoryService;
    @Mock private TaskMapper taskMapper;

    @InjectMocks private TaskServiceImpl taskService;

    private User currentUser;

    @BeforeEach
    void setUp() {
        currentUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .build();
        lenient().when(jwtService.getCurrentUser()).thenReturn(currentUser);
    }

    @Test
    void createTask_ShouldSaveAndReturnDto() {
        TaskCreationOrUpdateRequest request = TaskCreationOrUpdateRequest.builder()
                .title("Test Task")
                .description("Desc")
                .categoryId(2L)
                .dueDate(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();

        Category category = Category.builder()
                .id(2L)
                .build();
        when(categoryService.getCategoryById(2L)).thenReturn(category);

        Task savedTask = Task.builder()
                .id(1L)
                .title("Test Task")
                .build();
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        TaskResponseDTO dto = new TaskResponseDTO();
        when(taskMapper.toDto(savedTask)).thenReturn(dto);

        TaskResponseDTO result = taskService.createTask(request);
        assertThat(result).isEqualTo(dto);
    }

    @Test
    void getTaskById_ShouldReturnTask_WhenOwnedByUser() {
        Task task = Task.builder()
                .id(1L)
                .build();
        task.setUser(currentUser);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        Task result = taskService.getTaskById(1L);
        assertThat(result).isEqualTo(task);
    }

    @Test
    void getTaskById_ShouldThrow_WhenTaskNotFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(1L));
    }

    @Test
    void getTaskById_ShouldThrow_WhenUserNotOwner() {
        Task task = Task.builder()
                .id(1L)
                .build();
        User otherUser = User.builder()
                .id(2L)
                .build();
        task.setUser(otherUser);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(1L));
    }

    @Test
    void toggleTaskCompletion_ShouldFlipCompletedStatus() {
        Task task = Task.builder()
                .id(1L)
                .build();
        task.setUser(currentUser);
        task.setCompleted(false);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskResponseDTO dto = new TaskResponseDTO();
        when(taskMapper.toDto(task)).thenReturn(dto);

        TaskResponseDTO result = taskService.toggleTaskCompletion(1L);
        assertThat(task.isCompleted()).isTrue();
        assertThat(task.isReminderSent()).isTrue();
        assertThat(result).isEqualTo(dto);
    }

    @Test
    void toggleTaskNotification_ShouldFlipEmailEnabled() {
        Task task = Task.builder()
                .id(1L)
                .build();
        task.setUser(currentUser);
        task.setEmailEnabled(false);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskResponseDTO dto = new TaskResponseDTO();
        when(taskMapper.toDto(task)).thenReturn(dto);

        TaskResponseDTO result = taskService.toggleTaskNotification(1L);
        assertThat(task.isEmailEnabled()).isTrue();
        assertThat(result).isEqualTo(dto);
    }

    @Test
    void updateTask_ShouldUpdateTask() {
        TaskCreationOrUpdateRequest request = TaskCreationOrUpdateRequest.builder()
                .title("Updated")
                .categoryId(2L)
                .dueDate(Instant.now().plus(2, ChronoUnit.DAYS))
                .description("Desc")
                .build();

        Task existingTask = Task.builder()
                .id(1L)
                .user(currentUser)
                .dueDate(Instant.now().plus(1, ChronoUnit.DAYS))
                .build();
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));

        Category category = Category.builder().build();
        when(categoryService.getCategoryById(2L)).thenReturn(category);
        when(taskRepository.save(existingTask)).thenReturn(existingTask);

        TaskResponseDTO dto = TaskResponseDTO.builder().build();
        when(taskMapper.toDto(existingTask)).thenReturn(dto);

        TaskResponseDTO result = taskService.updateTask(1L, request);
        assertThat(result).isEqualTo(dto);
        assertThat(existingTask.getTitle()).isEqualTo("Updated");
        assertThat(existingTask.getCategory()).isEqualTo(category);
        assertThat(existingTask.isReminderSent()).isFalse();
    }

    @Test
    void deleteTasks_ShouldCallRepository() {
        List<Long> ids = List.of(1L, 2L);
        taskService.deleteTasks(ids);
        verify(taskRepository).deleteTasks(ids, currentUser.getId());
    }

    @Test
    void countTasks_ShouldReturnCount() {
        when(taskRepository.countTasks(currentUser.getId())).thenReturn(5);
        assertThat(taskService.countTasks()).isEqualTo(5);
    }

    @Test
    void countCompletedTasks_ShouldReturnCount() {
        when(taskRepository.countCompletedTasks(currentUser.getId())).thenReturn(3);
        assertThat(taskService.countCompletedTasks()).isEqualTo(3);
    }

    @Test
    void countTasksInCategory_ShouldReturnCount() {
        when(taskRepository.countTasksInCategory(currentUser.getId(), 2L)).thenReturn(2);
        assertThat(taskService.countTasksInCategory(2L)).isEqualTo(2);
    }

    @Test
    void getTasks_ShouldCallCorrectRepositoryMethod() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("dueDate").ascending());
        Page<Task> page = new PageImpl<>(List.of(Task.builder().build()));
        when(taskRepository.findAllByUserId(currentUser.getId(), pageable)).thenReturn(page);
        when(taskMapper.toDto(any())).thenReturn(new TaskResponseDTO());

        taskService.getTasks(null, "asc", "dueDate", null, 0);
        verify(taskRepository).findAllByUserId(currentUser.getId(), pageable);
    }

    @Test
    void searchTasks_ShouldCallRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> page = new PageImpl<>(List.of(Task.builder().build()));
        when(taskRepository.searchTasks(currentUser.getId(), "keyword", pageable)).thenReturn(page);
        when(taskMapper.toDto(any())).thenReturn(new TaskResponseDTO());

        taskService.searchTasks("keyword", 0);
        verify(taskRepository).searchTasks(currentUser.getId(), "keyword", pageable);
    }
}
