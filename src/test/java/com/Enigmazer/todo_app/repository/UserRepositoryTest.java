package com.Enigmazer.todo_app.repository;

import com.Enigmazer.todo_app.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    User user;

    @BeforeEach
    void setup() {
        user = userRepository.save(
                User.builder()
                        .name("Test User")
                        .email("test@example.com")
                        .password("password")
                        .provider("google")
                        .providerId("google-123")
                        .build()
        );
    }

    @Test
    @DisplayName("Should find user by ID")
    void shouldFindUserById() {
        Optional<User> found = userRepository.findById(user.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
        Optional<User> found = userRepository.findByEmail("test@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test User");
    }

    @Test
    @DisplayName("Should find user by provider and providerId")
    void shouldFindByProviderAndProviderId() {
        Optional<User> found = userRepository.findByProviderAndProviderId("google", "google-123");

        assertThat(found).isPresent();
        assertThat(found.get().getProvider()).isEqualTo("google");
    }

    @Test
    @DisplayName("Should return true when user exists by email")
    void shouldReturnTrueIfUserExistsByEmail() {
        boolean exists = userRepository.existsByEmail("test@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false when user email does not exist")
    void shouldReturnFalseIfUserDoesNotExistByEmail() {
        boolean exists = userRepository.existsByEmail("fake@example.com");

        assertThat(exists).isFalse();
    }
}
