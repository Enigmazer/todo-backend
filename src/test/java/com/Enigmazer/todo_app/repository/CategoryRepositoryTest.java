package com.Enigmazer.todo_app.repository;

import com.Enigmazer.todo_app.model.Category;
import com.Enigmazer.todo_app.model.Task;
import com.Enigmazer.todo_app.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private Category globalCategory;
    private Category user1Category;

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

        globalCategory = categoryRepository.save(
                Category.builder().name("Global").isGlobal(true).user(null).build()
        );
        user1Category = categoryRepository.save(
                Category.builder().name("Personal").isGlobal(false).user(user1).build()
        );
    }

    @Test
    void shouldReturnAvailableCategories() {
        List<Category> result = categoryRepository.findAvailableCategories(user1.getId());

        assertThat(result)
                .extracting(Category::getName)
                .containsExactlyInAnyOrder("Global", "Personal");
    }

    @Test
    void shouldReturnOnlyGlobalForOtherUser() {
        List<Category> result = categoryRepository.findAvailableCategories(user2.getId());

        assertThat(result)
                .extracting(Category::getName)
                .containsExactly("Global");
    }

    @Test
    void shouldFindByIdAndUserId() {
        Optional<Category> global = categoryRepository.findByIdAndUserId(globalCategory.getId(), user2.getId());
        Optional<Category> owned = categoryRepository.findByIdAndUserId(user1Category.getId(), user1.getId());

        assertThat(global).isPresent();
        assertThat(owned).isPresent();
    }

    @Test
    void shouldNotFindIfNotAccessible() {
        Optional<Category> result = categoryRepository.findByIdAndUserId(user1Category.getId(), user2.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void shouldFindGlobalByName() {
        Optional<Category> result = categoryRepository.findByNameAndGlobal("Global");

        assertThat(result).isPresent();
        assertThat(result.get().isGlobal()).isTrue();
    }

    @Test
    void shouldCountTasksByCategoryId() {
        Task t1 = taskRepository.save(
                Task.builder()
                        .title("t1")
                        .dueDate(Instant.now().plusSeconds(60 * 60))
                        .category(globalCategory)
                        .user(user1)
                        .build());
        Task t2 = taskRepository.save(
                Task.builder()
                        .title("t2")
                        .dueDate(Instant.now().plusSeconds(60 * 60))
                        .category(globalCategory)
                        .user(user1)
                        .build());

        Long count = categoryRepository.countTasksByCategoryId(globalCategory.getId());
        assertThat(count).isEqualTo(2L);
    }

    @Test
    void shouldCountAccessibleCategories() {
        Integer count = categoryRepository.countCategories(user1.getId());

        assertThat(count).isEqualTo(2); // 1 global + 1 personal
    }
}
