package com.Enigmazer.todo_app.controller;

import com.Enigmazer.todo_app.dto.user.UserResponseDTO;
import com.Enigmazer.todo_app.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(){
        UserResponseDTO user = userService.getCurrentUser();
        log.info("current user returned successfully");
        return ResponseEntity.ok(user);
    }

}
