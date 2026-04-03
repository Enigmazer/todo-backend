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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager manager;
    private final JWTService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public TokenPair login(UserLoginRequest dto) {
        log.info("User with email {} is trying to log in", dto.getEmail());
        manager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );
        return jwtService.generateTokens(dto.getEmail());
    }

    @Override
    @Transactional
    public void logout(String refreshToken){
        refreshTokenRepository.deleteByToken(refreshToken);
        log.info("User logged out successfully");
    }

    @Override
    @Transactional
    public void changePassword(UserLoginRequest request) {
        User currentUser = jwtService.getCurrentUser();
        compareEmail(currentUser.getEmail(), request.getEmail());

        currentUser.setPassword(passwordEncoder.encode(request.getPassword()));
        currentUser.setUpdatedAt(Instant.now());
        userRepository.save(currentUser);
        log.info("Password successfully updated for: {}", currentUser.getEmail());
    }

    @Override
    public String getLatestRefreshTokenForUser() {
        return refreshTokenRepository
                .findFirstByUserIdOrderByCreatedAtDesc(jwtService.getCurrentUser().getId())
                .map(RefreshToken::getToken)
                .orElseThrow(() -> new ResourceNotFoundException("No refresh token found for this user."));
    }

    public void compareEmail(String realEmail, String compareEmail) {
        if (!realEmail.equals(compareEmail)) {
            log.error("Email mismatch: current user={}, requested={}", realEmail, compareEmail);
            throw new AccessDeniedException("Email mismatch.");
        }
    }
}
