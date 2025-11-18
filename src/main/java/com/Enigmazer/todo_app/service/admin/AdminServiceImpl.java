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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * AdminServiceImpl implements {@link AdminService} and
 * here we have written admin related logic
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService{

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    /**
     * create a new user object and map the details
     * with the dto, checks does email already exists in db,
     * encode the password and save the user
     *
     * @param dto DTO containing email and password
     * @throws IllegalStateException if the email already exists in db
     */
    @Override
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
                        .roles(Set.of(RoleConstants.ROLE_ADMIN))
                        .provider("local")
                        .build()
        );
        log.info("User: {} is successfully saved and registered", email);
    }
    
    /**
     * Changes account status to enable/disable or
     * throw exception when user not found
     *
     * @param userId ID of the user to modify
     * @param status true to enable, false to disable
     * @throws ResourceNotFoundException when provided user
     * id is not found in the database
     */
    @Override
    public void updateUserAccountStatus(long userId, boolean status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        user.setEnabled(status);
        userRepository.save(user);
    }

    /**
     * delete the user with provided id from the database
     *
     * @param userId ID of the user to delete
     */
    @Override
    public void deleteUser(long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        userRepository.delete(user);
    }

    /**
     * returns all users from database
     *
     * @return list of all user from database
     */
    @Override
    public List<User> getUsers() {
        log.info("returning the list of all users");
        return userRepository.findAll();
    }

    /**
     * adds a global category which is visible to everyone and this category have null
     * userid mean no single user own it because it's for everyone, and it requires admin access to delete
     *
     * @param category DTO containing category data
     * @return the category which we just created
     * @throws DuplicateResourceException if the category already exists
     */
    @Override
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
