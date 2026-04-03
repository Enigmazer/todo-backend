package com.Enigmazer.todo_app.service.user;

import com.Enigmazer.todo_app.dto.user.UserResponseDTO;
import com.Enigmazer.todo_app.mapper.UserMapper;
import com.Enigmazer.todo_app.service.JWTService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final JWTService jwtService;
    private final UserMapper userMapper;

    @Override
    public UserResponseDTO getCurrentUser() {
        log.info("Fetching details of the current user");
        return userMapper.toDto(jwtService.getCurrentUser());
    }
}
