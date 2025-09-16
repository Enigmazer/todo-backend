package com.Enigmazer.todo_app.service.user;

import com.Enigmazer.todo_app.dto.user.UserResponseDTO;
import com.Enigmazer.todo_app.mapper.UserMapper;
import com.Enigmazer.todo_app.service.JWTService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * UserServiceImpl is the implementation of {@link UserService} that
 * handles business logic for user data retrieval.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final JWTService jwtService;
    private final UserMapper userMapper;


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
