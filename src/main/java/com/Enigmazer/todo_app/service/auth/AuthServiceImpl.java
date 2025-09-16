package com.Enigmazer.todo_app.service.auth;

import com.Enigmazer.todo_app.dto.token.TokenPair;
import com.Enigmazer.todo_app.dto.user.UserLoginRequest;
import com.Enigmazer.todo_app.exception.CustomExceptions.ResourceNotFoundException;
import com.Enigmazer.todo_app.model.RefreshToken;
import com.Enigmazer.todo_app.model.User;
import com.Enigmazer.todo_app.repository.RefreshTokenRepository;
import com.Enigmazer.todo_app.repository.UserRepository;
import com.Enigmazer.todo_app.service.JWTService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * AuthServiceImpl is the implementation of {@link AuthService} that
 * handles business logic for user authentication and password management.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager manager;
    private final JWTService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Verifies the provided credentials and return a token pair if valid.
     *
     * @param dto a {@link UserLoginRequest} containing login details
     * @return Token pair containing access and refresh token
     */
    @Override
    public TokenPair login(UserLoginRequest dto) {
        log.info("User with email {} is trying to log in", dto.getEmail());
        manager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );
        return jwtService.generateTokens(dto.getEmail());
    }

    /**
     * Takes the accessToken and invalidate both
     * access and refresh token using {@link JWTService}
     * so anyone won't be able to use those same tokens
     * again for accessing any end point ensuring
     * secure logout
     *
     * @param accessToken the access token to invalidate
     */
    @Override
    public void logout(String accessToken){
        String email = jwtService.getCurrentUser().getEmail();
        log.info("logging out the user: {}", email);

        if (accessToken != null && !accessToken.isBlank()) {
            jwtService.invalidateTokens(accessToken);
            log.info("Tokens are invalidated for user: {}", email);
        }
        log.info("User {} is successfully logged out.", email);
    }

    /**
     * Changes the password for the current user.
     *
     * @param request contains email and new password
     */
    @Override
    public void changePassword(UserLoginRequest request) {
        log.info("Password change request for: {}", request.getEmail());
        User currentUser = jwtService.getCurrentUser();
        compareEmail(currentUser.getEmail(), request.getEmail());

        currentUser.setPassword(passwordEncoder.encode(request.getPassword()));
        currentUser.setUpdatedAt(Instant.now());
        userRepository.save(currentUser);
        log.info("Password successfully updated for: {}", currentUser.getEmail());
    }

    /**
     * Retrieves the latest refresh token for the current user
     * @return refresh token string
     * @throws ResourceNotFoundException if no refresh token is found for the user
     */
    @Override
    public String getLatestRefreshTokenForUser() {
        return refreshTokenRepository
                .findFirstByUserIdOrderByCreatedAtDesc(jwtService.getCurrentUser().getId())
                .map(RefreshToken::getToken)
                .orElseThrow(() -> new ResourceNotFoundException("No refresh token found for this user."));
    }

    /**
     * Ensures the email making the request matches the currently logged-in user.
     *
     * @param realEmail email of the logged-in user
     * @param compareEmail email from the request
     * @throws AccessDeniedException if the emails don't match
     */
    public void compareEmail(String realEmail, String compareEmail) {
        if (!realEmail.equals(compareEmail)) {
            log.error("Email mismatch: current user={}, requested={}", realEmail, compareEmail);
            throw new AccessDeniedException("Email mismatch.");
        }
    }
}
