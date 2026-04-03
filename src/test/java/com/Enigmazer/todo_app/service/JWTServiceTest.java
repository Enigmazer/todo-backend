package com.Enigmazer.todo_app.service;

import com.Enigmazer.todo_app.dto.token.TokenPair;
import com.Enigmazer.todo_app.model.RefreshToken;
import com.Enigmazer.todo_app.model.User;
import com.Enigmazer.todo_app.repository.RefreshTokenRepository;
import com.Enigmazer.todo_app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JWTServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private JWTService jwtService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 1. Initialize the testUser and assign necessary fields
        testUser = User.builder()
                .email("test@example.com")
                .roles(Set.of("ROLE_USER"))
                .build();

        // 2. Set up the service
        jwtService = new JWTService(userRepository, refreshTokenRepository);

        ReflectionTestUtils.setField(jwtService, "secretKey",
                Base64.getEncoder().encodeToString("this-is-a-very-secure-test-key-1234567890".getBytes())
        );

        ReflectionTestUtils.setField(jwtService, "accessTokenExpiry", Duration.ofMinutes(5));
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiry", Duration.ofDays(7));

        // 3. manually trigger @PostConstruct
        ReflectionTestUtils.invokeMethod(jwtService, "initKey");
    }

    // Token Generation Tests
    @Test
    void generateTokens_ShouldReturnTokenPair_AndUpdateLastLogin() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        TokenPair tokenPair = jwtService.generateTokens("test@example.com");

        assertNotNull(tokenPair);
        assertNotNull(tokenPair.accessToken());
        assertNotNull(tokenPair.refreshToken());
        verify(userRepository).save(testUser);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    // Token Validation Tests
    @Test
    void isAccessTokenValid_ShouldReturnTrue_ForValidToken() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        String token = jwtService.generateTokens("test@example.com").accessToken();
        assertTrue(jwtService.isAccessTokenValid(token));
    }

    @Test
    void isAccessTokenValid_ShouldReturnFalse_ForMalformedToken() {
        assertFalse(jwtService.isAccessTokenValid("not.a.real.token"));
    }

    // Claim Extraction Tests
    @Test
    void extractUsername_ShouldReturnCorrectEmail() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        String token = jwtService.generateTokens("test@example.com").accessToken();
        String username = jwtService.extractUsername(token);

        assertEquals("test@example.com", username);
    }

    @Test
    void extractRole_ShouldReturnCorrectRoles() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        String token = jwtService.generateTokens("test@example.com").accessToken();
        Set<String> roles = jwtService.extractRole(token);

        assertTrue(roles.contains("ROLE_USER"));
    }

    // Refresh Token Tests
    @Test
    void isRefreshTokenValid_ShouldReturnTrue_WhenNotExpiredOrRevoked() {
        RefreshToken refreshToken = new RefreshToken("validToken", testUser, Instant.now(), Instant.now().plus(Duration.ofDays(1)));
        refreshToken.setRevoked(false);

        when(refreshTokenRepository.findByToken("validToken")).thenReturn(Optional.of(refreshToken));

        assertTrue(jwtService.isRefreshTokenValid("validToken"));
    }

    @Test
    void isRefreshTokenValid_ShouldReturnFalse_WhenExpired() {
        RefreshToken expiredToken = new RefreshToken("expired", testUser, Instant.now().minus(Duration.ofDays(2)), Instant.now().minus(Duration.ofDays(1)));
        expiredToken.setRevoked(false);

        when(refreshTokenRepository.findByToken("expired")).thenReturn(Optional.of(expiredToken));

        assertFalse(jwtService.isRefreshTokenValid("expired"));
    }

    @Test
    void renewTokens_ShouldThrowException_ForInvalidRefreshToken() {
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> jwtService.renewTokens("invalid"));
    }

    @Test
    void invalidateRefreshToken_ShouldMarkTokenRevoked() {
        RefreshToken refreshToken = new RefreshToken("token", testUser, Instant.now(), Instant.now().plus(Duration.ofDays(1)));
        refreshToken.setRevoked(false);
        when(refreshTokenRepository.findByToken("token")).thenReturn(Optional.of(refreshToken));

        jwtService.invalidateRefreshToken("token");

        assertTrue(refreshToken.isRevoked());
    }

    @Test
    void invalidateRefreshToken_ShouldHandleMissingTokenGracefully() {
        when(refreshTokenRepository.findByToken("missing")).thenReturn(Optional.empty());
        assertDoesNotThrow(() -> jwtService.invalidateRefreshToken("missing"));
    }

    // Utility & Error Handling Tests
    @Test
    void getCurrentUser_ShouldThrow_WhenNoUserFound() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(authentication.getName()).thenReturn("unknown@example.com");
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> jwtService.getCurrentUser());
    }

    @Test
    void extractExpirationDate_ShouldReturnFutureDate() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        String token = jwtService.generateTokens("test@example.com").accessToken();
        Date expiration = jwtService.extractExpirationDate(token);

        assertTrue(expiration.after(new Date()));
    }

    @Test
    void isAccessTokenValid_ShouldReturnFalse_ForExpiredToken() {
        // Make access token expiry short for this test
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiry", Duration.ofMillis(1));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        String token = jwtService.generateTokens("test@example.com").accessToken();

        // Wait for expiration
        try { Thread.sleep(5); } catch (InterruptedException ignored) {}

        assertFalse(jwtService.isAccessTokenValid(token));
    }
}
