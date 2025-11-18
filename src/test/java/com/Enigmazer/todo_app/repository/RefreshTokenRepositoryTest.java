package com.Enigmazer.todo_app.repository;

import com.Enigmazer.todo_app.model.RefreshToken;
import com.Enigmazer.todo_app.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = userRepository.save(
                User.builder()
                        .name("user1")
                        .email("user1@example.com")
                        .password("password")
                        .provider("local")
                        .build());
        user2 = userRepository.save(
                User.builder()
                        .name("user2")
                        .email("user2@example.com")
                        .password("password")
                        .provider("local")
                        .build());

        RefreshToken token1 = refreshTokenRepository.save(
                RefreshToken.builder()
                        .token("token-abc")
                        .user(user1)
                        .createdAt(Instant.now().minusSeconds(3600))
                        .expiry(Instant.now().plusSeconds(3600))
                        .build()
        );

        RefreshToken token2 = refreshTokenRepository.save(
                RefreshToken.builder()
                        .token("token-def")
                        .createdAt(Instant.now())
                        .expiry(Instant.now().plusSeconds(3600))
                        .user(user1)
                        .build()
        );

        RefreshToken token3 = refreshTokenRepository.save(
                RefreshToken.builder()
                        .token("token-xyz")
                        .createdAt(Instant.now())
                        .expiry(Instant.now().plusSeconds(3600))
                        .user(user2)
                        .build()
        );
    }

    @Test
    @DisplayName("findByToken() should return the matching refresh token")
    void shouldFindByToken() {
        Optional<RefreshToken> found = refreshTokenRepository.findByToken("token-def");

        assertThat(found).isPresent();
        assertThat(found.get().getUser().getId()).isEqualTo(user1.getId());
    }

    @Test
    @DisplayName("findUserByToken() should return the user linked to a token")
    void shouldFindUserByToken() {
        Optional<User> foundUser = refreshTokenRepository.findUserByToken("token-xyz");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(user2.getId());
    }

    @Test
    @DisplayName("findFirstByUserIdOrderByCreatedAtDesc() should return the latest token for user")
    void shouldReturnLatestTokenForUser() {
        Optional<RefreshToken> latest = refreshTokenRepository.findFirstByUserIdOrderByCreatedAtDesc(user1.getId());

        assertThat(latest).isPresent();
        assertThat(latest.get().getToken()).isEqualTo("token-def");
    }

    @Test
    @DisplayName("deleteByToken() should remove the token from database")
    void shouldDeleteByToken() {
        refreshTokenRepository.deleteByToken("token-abc");

        Optional<RefreshToken> deleted = refreshTokenRepository.findByToken("token-abc");
        assertThat(deleted).isEmpty();
    }

    @Test
    @DisplayName("deleteByUser() should remove all tokens for a specific user")
    void shouldDeleteByUser() {
        refreshTokenRepository.deleteByUser(user1);

        assertThat(refreshTokenRepository.findByToken("token-abc")).isEmpty();
        assertThat(refreshTokenRepository.findByToken("token-def")).isEmpty();
        assertThat(refreshTokenRepository.findByToken("token-xyz")).isPresent(); // user2's token still exists
    }
}
