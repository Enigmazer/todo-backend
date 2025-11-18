package com.Enigmazer.todo_app.service.admin;

import com.Enigmazer.todo_app.constants.RoleConstants;
import com.Enigmazer.todo_app.dto.category.CategoryCreationRequest;
import com.Enigmazer.todo_app.dto.category.CategoryResponseDTO;
import com.Enigmazer.todo_app.dto.user.UserRegistrationRequest;
import com.Enigmazer.todo_app.exception.CustomExceptions.DuplicateResourceException;
import com.Enigmazer.todo_app.exception.CustomExceptions.ResourceNotFoundException;
import com.Enigmazer.todo_app.mapper.CategoryMapper;
import com.Enigmazer.todo_app.model.Category;
import com.Enigmazer.todo_app.model.User;
import com.Enigmazer.todo_app.repository.CategoryRepository;
import com.Enigmazer.todo_app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private BCryptPasswordEncoder passwordEncoder;
    @Mock private CategoryRepository categoryRepository;
    @Mock private CategoryMapper categoryMapper;
    @InjectMocks private AdminServiceImpl adminService;

    @Test
    void createAdmin_ShouldSaveUser_WhenEmailNotExists() {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .name("Admin")
                .email("admin@example.com")
                .password("password")
                .build();

        when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        adminService.createAdmin(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User savedUser = captor.getValue();

        assertThat(savedUser.getEmail()).isEqualTo("admin@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("encodedPassword");
        assertThat(savedUser.getRoles()).isEqualTo(Set.of(RoleConstants.ROLE_ADMIN));
        assertThat(savedUser.getProvider()).isEqualTo("local");
    }

    @Test
    void createAdmin_ShouldThrowException_WhenEmailExists() {
        UserRegistrationRequest request = UserRegistrationRequest.builder()
                .email("admin@example.com")
                .build();

        when(userRepository.existsByEmail("admin@example.com")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> adminService.createAdmin(request));
    }

    @Test
    void updateUserAccountStatus_ShouldEnableUser_WhenUserExists() {
        User user = User.builder()
                .isEnabled(false)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        adminService.updateUserAccountStatus(1L, true);

        assertThat(user.isEnabled()).isTrue();
        verify(userRepository).save(user);
    }

    @Test
    void updateUserAccountStatus_ShouldThrow_WhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> adminService.updateUserAccountStatus(1L, true));
    }

    @Test
    void deleteUser_ShouldDeleteUser_WhenExists() {
        User user = User.builder().build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        adminService.deleteUser(1L);

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_ShouldThrow_WhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> adminService.deleteUser(1L));
    }

    @Test
    void getUsers_ShouldReturnAllUsers() {
        User user1 = User.builder().build();
        User user2 = User.builder().build();

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<User> result = adminService.getUsers();
        assertThat(result).containsExactly(user1, user2);
    }

    @Test
    void createGlobalCategory_ShouldSaveCategory_WhenNotExists() {
        CategoryCreationRequest request = CategoryCreationRequest.builder()
                .name("Work")
                .build();

        when(categoryRepository.findByNameAndGlobal("Work")).thenReturn(Optional.empty());
        Category category = Category.builder()
                .name("Work")
                .build();
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        CategoryResponseDTO dto = CategoryResponseDTO.builder()
                .name("Work")
                .build();
        when(categoryMapper.toDto(category)).thenReturn(dto);

        CategoryResponseDTO result = adminService.createGlobalCategory(request);
        assertThat(result.getName()).isEqualTo("Work");
    }

    @Test
    void createGlobalCategory_ShouldThrow_WhenCategoryExists() {
        CategoryCreationRequest request = CategoryCreationRequest.builder()
                .name("Work")
                .build();
        when(categoryRepository.findByNameAndGlobal("Work")).thenReturn(Optional.of(Category.builder().build()));

        assertThrows(DuplicateResourceException.class, () -> adminService.createGlobalCategory(request));
    }
}
