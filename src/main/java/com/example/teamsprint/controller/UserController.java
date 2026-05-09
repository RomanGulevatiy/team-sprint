package com.example.teamsprint.controller;

import com.example.teamsprint.dto.RegisterRequest;
import com.example.teamsprint.dto.UserResponse;
import com.example.teamsprint.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        return userService.register(registerRequest);
    }

    @PatchMapping("/projects/{projectId}/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse assignUserToProject(@PathVariable Long userId, @PathVariable Long projectId) {
        return userService.assignUserToProject(userId, projectId);
    }
}
