package com.Enigmazer.todo_app.service.admin;

import com.Enigmazer.todo_app.dto.category.CategoryCreationRequest;
import com.Enigmazer.todo_app.dto.category.CategoryResponseDTO;
import com.Enigmazer.todo_app.dto.user.UserRegistrationRequest;
import com.Enigmazer.todo_app.enums.RoleType;
import com.Enigmazer.todo_app.exception.CustomExceptions.DuplicateResourceException;
import com.Enigmazer.todo_app.exception.CustomExceptions.ResourceNotFoundException;
import com.Enigmazer.todo_app.mapper.CategoryMapper;
import com.Enigmazer.todo_app.model.Category;
import com.Enigmazer.todo_app.model.User;
import com.Enigmazer.todo_app.repository.CategoryRepository;
import com.Enigmazer.todo_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminServiceImpl implements AdminService{

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public void createAdmin(UserRegistrationRequest dto) {

        String email = dto.getEmail().toLowerCase();
        log.info("Attempting to register admin user with email: {}", email);

        if (userRepository.existsByEmail(email)) {
            log.warn("Email already in use : {}", email);
            throw new IllegalStateException("Email is already in use");
        }

        log.info("Mapping the details from dto to real user object");
        userRepository.save(
                User.builder()
                        .name(dto.getName())
                        .email(email)
                        .password(passwordEncoder.encode(dto.getPassword()))
                        .roles(Set.of(RoleType.ADMIN.toString()))
                        .provider("local")
                        .build()
        );
        log.info("User: {} is successfully saved and registered", email);
    }

    @Override
    @Transactional
    public void updateUserAccountStatus(long userId, boolean status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        user.setEnabled(status);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        userRepository.delete(user);
    }

    @Override
    public List<User> getUsers() {
        log.info("returning the list of all users");
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public CategoryResponseDTO createGlobalCategory(CategoryCreationRequest category){
        categoryRepository.findByNameAndGlobal(
                category.getName()).ifPresent(
                        c -> {
                            log.error("Category: {} already exist in the database ", category.getName());
                            throw new DuplicateResourceException("Category already exists");
                        }
        );

        Category globalCategory = categoryRepository.save(
                Category.builder()
                        .name(category.getName())
                        .user(null)
                        .isGlobal(true)
                        .build()
        );

        log.error("Category: {} successfully added in the database ", globalCategory.getName());
        return categoryMapper.toDto(globalCategory);
    }

}
