package com.Enigmazer.todo_app.controller;

import com.Enigmazer.todo_app.dto.user.UserResponseDTO;
import com.Enigmazer.todo_app.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * UserController handles user related operations.
 * <p>
 * user controller handles operations like:
 * <ul>
 *     <li>Returning a self object to display profile details</li>
 * </ul>
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    /**
     * returns an object containing public
     * details of the logged-in user
     *
     * @return details of logged-in user
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(){
        UserResponseDTO user = userService.getCurrentUser();
        log.info("current user returned successfully");
        return ResponseEntity.ok(user);
    }

}
