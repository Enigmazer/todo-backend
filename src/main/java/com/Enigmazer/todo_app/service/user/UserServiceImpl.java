package com.Enigmazer.todo_app.service.user;

import com.Enigmazer.todo_app.dto.token.TokenPair;
import com.Enigmazer.todo_app.dto.user.UserLoginRequest;
import com.Enigmazer.todo_app.dto.user.UserResponseDTO;
import com.Enigmazer.todo_app.mapper.UserMapper;
import com.Enigmazer.todo_app.model.User;
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
 * UserServiceImpl is the implementation of {@link UserService} that
 * handles business logic for user authentication, password management,
 * and user data retrieval.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager manager;
    private final JWTService jwtService;
    private final UserMapper userMapper;

    /**
     * Verifies the provided credentials and generates a JWT if valid.
     *
     * @param dto a {@link UserLoginRequest} containing login details
     * @return a signed JWT token
     */
    @Override
    public TokenPair verify(UserLoginRequest dto) {
        log.info("User with email {} is trying to log in", dto.getEmail());
        Authentication authentication = manager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );

        return jwtService.generateTokens(dto.getEmail());
    }

    /**
     * Takes the accessToken and invalidate both
     * access and refresh token using {@link JWTService}
     * so anyone won't be able to use that same tokens
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

//    /**
//     * Checks if the currently authenticated user has a password already set.
//     *
//     * @return true if a password is set, false otherwise
//     */
//    @Override
//    public boolean isPasswordAvailable() {
//        log.info("Checking if this account has a password set");
//        String password = jwtService.getCurrentUser().getPassword();
//        return password != null && !password.isBlank();
//    }

//    /**
//     * Sets a password for the currently authenticated user if they don't already have one.
//     *
//     * @param request the password to set, wrapped in {@link UserLoginRequest}
//     */
//    @Override
//    public void setAPassword(@Valid UserLoginRequest request) {
//        log.info("Password set request for user: {}", request.getEmail());
//        User currentUser = jwtService.getCurrentUser();
//
//        compareEmail(currentUser.getEmail(), request.getEmail());
//
//        String pass = currentUser.getPassword();
//        if (pass != null && !pass.isBlank()) {
//            log.error("User already has a password set: {}", currentUser.getEmail());
//            throw new IllegalArgumentException("You already have a password set.");
//        }
//
//        currentUser.setPassword(passwordEncoder.encode(request.getPassword()));
//        currentUser.setUpdatedAt(Instant.now());
//        userRepository.save(currentUser);
//        log.info("Password set successfully for: {}", request.getEmail());
//    }

    /**
     * Changes the password for the current user after verifying the old password.
     *
     * @param request contains email, old password, and new password
     */
    @Override
    public void changePassword(UserLoginRequest request) {
        log.info("Password change request for: {}", request.getEmail());
        User currentUser = jwtService.getCurrentUser();
        compareEmail(currentUser.getEmail(), request.getEmail());

//        if (!passwordEncoder.matches(request.getOldPassword(), currentUser.getPassword())) {
//            log.error("Old password is incorrect for: {}", currentUser.getEmail());
//            throw new IllegalArgumentException("Old password is incorrect.");
//        }

        currentUser.setPassword(passwordEncoder.encode(request.getPassword()));
        currentUser.setUpdatedAt(Instant.now());
        userRepository.save(currentUser);
        log.info("Password successfully updated for: {}", currentUser.getEmail());
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

    /**
     * Returns the public-facing details of the currently logged-in user.
     *
     * @return {@link UserResponseDTO} containing user's metadata
     */
    @Override
    public UserResponseDTO getCurrentUser() {
        log.info("Fetching details of the current user");
        return userMapper.toDto(jwtService.getCurrentUser());
    }
}
