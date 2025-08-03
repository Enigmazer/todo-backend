package com.Enigmazer.todo_app.controller;

import com.Enigmazer.todo_app.dto.category.CategoryCreationRequest;
import com.Enigmazer.todo_app.dto.category.CategoryResponseDTO;
import com.Enigmazer.todo_app.dto.user.UserLoginRequest;
import com.Enigmazer.todo_app.dto.user.UserRegistrationRequest;
import com.Enigmazer.todo_app.model.User;
import com.Enigmazer.todo_app.service.admin.AdminServiceImpl;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import java.util.List;

/**
 * AdminController handles all admin-specific endpoints.
 * <p>
 * Requires the user to have <strong>ROLE_ADMIN</strong> authority.
 * <p>
 * All endpoints are secured using JWT and managed via {@link AdminServiceImpl}.
 * <p>
 * This controller contains operation like:
 * <ul>
 *     <li>Registering a new admin</li>
 *     <li>Enabling or disabling a user's account</li>
 *     <li>deleting a user account</li>
 *     <li>fetching all users from the database</li>
 *     <li>creating a global category</li>
 * </ul>
 */
@RestController
@RequestMapping("/admin")
@Validated
@Slf4j
public class AdminController {

    private final AdminServiceImpl adminService;

    @Autowired
    public AdminController(AdminServiceImpl adminService) {
        this.adminService = adminService;
    }

    /**
     * register method takes a login request dto and passes it to admin
     * for registration and only on a successful registration return a
     * confirmation message
     *
     * @param request   contains email and password
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody UserRegistrationRequest request) {
        log.info("new admin registration request for {}", request.getEmail());
        adminService.registerUser(request);
        log.info("admin registration for {} is successful", request.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Admin registered successfully");
    }

    /**
     * changeUserAccountStatus takes the user id
     * and enable or disable their account
     *
     * @param userId    id of the user whose account status we have to change
     * @param status    status which we want to set, true for enable
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/user/{userId}/status")
    public ResponseEntity<String> changeUserAccountStatus(@PathVariable long userId,@RequestParam boolean status){
        log.info("changing the status of user account with id {} to {}", userId, status);
        adminService.changeAccountStatus(userId, status);
        log.info("successfully changes the status of user account with id {} to {}", userId, status);
        return ResponseEntity.ok("User account status has been updated");
    }

    /**
     * deleteUser deletes the user's account
     *
     * @param userId    id of the account we want to delete
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/user/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable long userId){
        log.info("deleting the user account with id: {}", userId);
        adminService.deleteUser(userId);
        log.info("user account with id: {} , is successfully deleted", userId);
        return ResponseEntity.ok("User with id " + userId + " is successfully deleted from the database");
    }

    /**
     * Retrieves all user accounts from the database.
     *
     * @return list of all registered users
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("fetching all the users from the database");
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    /**
     * addGlobalCategory creates a new global category in the database
     * which is available for all user and is not deletable by a normal user
     *
     * @param category  a dto which contains a category as a string
     * @return  the created category
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/addGlobalCategory")
    public ResponseEntity<CategoryResponseDTO> addGlobalCategory(@Valid @RequestBody CategoryCreationRequest category){
        log.info("Adding a new global category: {}", category.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.addGlobalCategory(category));
    }
}
