package com.Enigmazer.todo_app.service.user;

import com.Enigmazer.todo_app.dto.user.PasswordChangeRequest;
import com.Enigmazer.todo_app.dto.user.UserLoginRequest;
import com.Enigmazer.todo_app.dto.user.UserResponseDTO;
import com.Enigmazer.todo_app.mapper.UserMapper;
import com.Enigmazer.todo_app.model.User;
import com.Enigmazer.todo_app.repository.UserRepository;
import com.Enigmazer.todo_app.service.JWTService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Test
    public void testAdd(){
        assertEquals(4,2+2);
    }
}