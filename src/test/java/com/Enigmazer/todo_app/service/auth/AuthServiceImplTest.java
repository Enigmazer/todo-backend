package com.Enigmazer.todo_app.service.auth;

import com.Enigmazer.todo_app.dto.token.TokenPair;
import com.Enigmazer.todo_app.dto.user.UserLoginRequest;
import com.Enigmazer.todo_app.exception.CustomExceptions.ResourceNotFoundException;
import com.Enigmazer.todo_app.model.RefreshToken;
import com.Enigmazer.todo_app.model.User;
import com.Enigmazer.todo_app.repository.RefreshTokenRepository;
import com.Enigmazer.todo_app.repository.UserRepository;
import com.Enigmazer.todo_app.service.JWTService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {

    private UserRepository userRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private AuthenticationManager manager;
    private JWTService jwtService;
    private RefreshTokenRepository refreshTokenRepository;
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(BCryptPasswordEncoder.class);
        manager = mock(AuthenticationManager.class);
        jwtService = mock(JWTService.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);

        authService = new AuthServiceImpl(userRepository, passwordEncoder, manager, jwtService, refreshTokenRepository);
    }

    @Test
    void login_ShouldAuthenticateAndReturnTokenPair() {
        UserLoginRequest request = UserLoginRequest.builder()
                .email("test@example.com")
                .password("password")
                .build();

        TokenPair tokenPair = new TokenPair("access", "refresh");
        when(jwtService.generateTokens(request.getEmail())).thenReturn(tokenPair);

        TokenPair result = authService.login(request);

        verify(manager).authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        assertThat(result).isEqualTo(tokenPair);
    }

    @Test
    void logout_ShouldInvalidateTokens() {
        String accessToken = "token";
        User user = User.builder()
                .email("user@example.com")
                .build();
        when(jwtService.getCurrentUser()).thenReturn(user);

        authService.logout(accessToken);

        verify(jwtService).invalidateTokens(accessToken);
    }

    @Test
    void changePassword_ShouldUpdatePassword() {
        UserLoginRequest request = UserLoginRequest.builder()
                .email("user@example.com")
                .password("newPassword")
                .build();

        User user = User.builder()
                .email("user@example.com")
                .build();
        when(jwtService.getCurrentUser()).thenReturn(user);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");

        authService.changePassword(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();
        assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
        assertThat(savedUser.getUpdatedAt()).isNotNull();
    }

    @Test
    void changePassword_ShouldThrowAccessDeniedException_WhenEmailMismatch() {
        UserLoginRequest request = UserLoginRequest.builder()
                .email("wrong@example.com")
                .password("newPassword")
                .build();

        User user = User.builder()
                .email("user@example.com")
                .build();
        when(jwtService.getCurrentUser()).thenReturn(user);

        assertThrows(AccessDeniedException.class, () -> authService.changePassword(request));
    }

    @Test
    void getLatestRefreshTokenForUser_ShouldReturnToken() {
        User user = User.builder()
                .id(1L)
                .build();
        when(jwtService.getCurrentUser()).thenReturn(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token("refreshTokenValue")
                .build();
        when(refreshTokenRepository.findFirstByUserIdOrderByCreatedAtDesc(user.getId()))
                .thenReturn(Optional.of(refreshToken));

        String token = authService.getLatestRefreshTokenForUser();
        assertThat(token).isEqualTo("refreshTokenValue");
    }

    @Test
    void getLatestRefreshTokenForUser_ShouldThrowResourceNotFound_WhenNoToken() {
        User user = User.builder()
                .id(1L)
                .build();
        when(jwtService.getCurrentUser()).thenReturn(user);

        when(refreshTokenRepository.findFirstByUserIdOrderByCreatedAtDesc(user.getId()))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.getLatestRefreshTokenForUser());
    }

    @Test
    void compareEmail_ShouldThrowAccessDeniedException_WhenEmailsDontMatch() {
        assertThrows(AccessDeniedException.class, () -> authService.compareEmail("real@example.com", "fake@example.com"));
    }

    @Test
    void compareEmail_ShouldNotThrow_WhenEmailsMatch() {
        authService.compareEmail("email@example.com", "email@example.com"); // no exception
    }
}
