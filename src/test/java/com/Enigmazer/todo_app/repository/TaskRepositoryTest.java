package com.Enigmazer.todo_app.repository;

import com.Enigmazer.todo_app.model.Category;
import com.Enigmazer.todo_app.model.Task;
import com.Enigmazer.todo_app.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User user1;
    private User user2;
    private Category catWork;
    private Category catHome;
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        user1 = userRepository.save(
                User.builder()
                        .name("user1")
                        .email("user1@example.com")
                        .password("password")
                        .provider("local")
                        .build()
        );
        user2 = userRepository.save(
                User.builder()
                        .name("user2")
                        .email("user2@example.com")
                        .password("password")
                        .provider("local")
                        .build()
        );

        catWork = categoryRepository.save(Category.builder().name("Work").isGlobal(false).user(user1).build());
        catHome = categoryRepository.save(Category.builder().name("Home").isGlobal(false).user(user1).build());

        task1 = taskRepository.save(
                Task.builder()
                        .title("Email boss")
                        .description("Send report")
                        .isCompleted(false)
                        .isEmailEnabled(true)
                        .isReminderSent(false)
                        .dueDate(Instant.now().plus(2, ChronoUnit.HOURS))
                        .category(catWork)
                        .user(user1)
                        .build()
        );

        task2 = taskRepository.save(
                Task.builder()
                        .title("Buy groceries")
                        .description("Eggs and milk")
                        .isCompleted(true)
                        .isEmailEnabled(false)
                        .isReminderSent(true)
                        .dueDate(Instant.now().plus(1, ChronoUnit.DAYS))
                        .category(catHome)
                        .user(user1)
                        .build()
        );

        task3 = taskRepository.save(
                Task.builder()
                        .title("Call friend")
                        .description("Birthday")
                        .isCompleted(false)
                        .isEmailEnabled(true)
                        .isReminderSent(false)
                        .dueDate(Instant.now().plus(3, ChronoUnit.HOURS))
                        .category(catWork)
                        .user(user2)
                        .build()
        );
    }

    @Test
    @DisplayName("findAllByUserId() should return only user's tasks")
    void shouldFindAllByUserId() {
        Page<Task> page = taskRepository.findAllByUserId(user1.getId(), PageRequest.of(0, 10));

        assertThat(page.getContent())
                .extracting(Task::getTitle)
                .containsExactlyInAnyOrder("Email boss", "Buy groceries");
    }

    @Test
    @DisplayName("findByIsCompletedAndUserId() should filter by completion status and user")
    void shouldFindByIsCompletedAndUserId() {
        Page<Task> completed = taskRepository.findByIsCompletedAndUserId(true, user1.getId(), PageRequest.of(0, 10));

        assertThat(completed.getContent()).extracting(Task::getTitle).containsExactly("Buy groceries");
    }

    @Test
    @DisplayName("findByUserIdAndCategoryId() should return tasks by category and user")
    void shouldFindByUserAndCategory() {
        Page<Task> result = taskRepository.findByUserIdAndCategoryId(user1.getId(), catHome.getId(), PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(Task::getTitle).containsExactly("Buy groceries");
    }

    @Test
    @DisplayName("searchTasks() should return tasks matching keyword in title or description")
    void shouldSearchTasks() {
        Page<Task> result = taskRepository.searchTasks(user1.getId(), "email", PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting(Task::getTitle).containsExactly("Email boss");
    }

    @Test
    @DisplayName("countTasks() should count total tasks for user")
    void shouldCountTasks() {
        Integer count = taskRepository.countTasks(user1.getId());
        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("countCompletedTasks() should count only completed tasks for user")
    void shouldCountCompletedTasks() {
        Integer count = taskRepository.countCompletedTasks(user1.getId());
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("countTasksInCategory() should count tasks for user in given category")
    void shouldCountTasksInCategory() {
        Integer count = taskRepository.countTasksInCategory(user1.getId(), catHome.getId());
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("deleteTasks() should delete selected tasks for a user only")
    void shouldDeleteTasksByIdsAndUser() {
        taskRepository.deleteTasks(List.of(task1.getId(), task2.getId(), task3.getId()), user1.getId());

        List<Task> remaining = taskRepository.findAll();
        assertThat(remaining)
                .extracting(Task::getTitle)
                .containsExactly("Call friend");
    }

    @Test
    @DisplayName("findRemindableTasks() should return only eligible tasks in range")
    void shouldFindRemindableTasks() {
        Instant start = Instant.now();
        Instant end = Instant.now().plus(5, ChronoUnit.HOURS);

        List<Task> remindable = taskRepository.findRemindableTasks(start, end);

        assertThat(remindable)
                .extracting(Task::getTitle)
                .containsExactlyInAnyOrder("Email boss", "Call friend");
        assertThat(remindable.getFirst().getUser()).isNotNull();
    }
}
