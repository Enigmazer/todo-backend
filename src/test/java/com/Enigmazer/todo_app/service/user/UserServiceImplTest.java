package com.Enigmazer.todo_app.service.user;

import com.Enigmazer.todo_app.dto.user.UserResponseDTO;
import com.Enigmazer.todo_app.mapper.UserMapper;
import com.Enigmazer.todo_app.model.User;
import com.Enigmazer.todo_app.service.JWTService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class UserServiceImplTest {

    @Mock
    JWTService jwtService;
    @Mock
    UserMapper userMapper;
    @InjectMocks
    UserServiceImpl userService;

    @Test
    void getCurrentUser_returnsMappedDto() {
        User mockUser = User.builder()
                .id(1L)
                .email("testUser")
                .build();

        UserResponseDTO dto = UserResponseDTO.builder()
                .email("testUser")
                .build();

        when(jwtService.getCurrentUser()).thenReturn(mockUser);
        when(userMapper.toDto(mockUser)).thenReturn(dto);

        UserResponseDTO result = userService.getCurrentUser();

        assertThat(result.getEmail()).isEqualTo("testUser");

        verify(jwtService).getCurrentUser();
        verify(userMapper).toDto(mockUser);
    }
}