package com.example.teamsprint.controller;

import com.example.teamsprint.dto.AuthResponse;
import com.example.teamsprint.dto.LoginRequest;
import com.example.teamsprint.dto.RegisterRequest;
import com.example.teamsprint.dto.UserResponse;
import com.example.teamsprint.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Management", description = "Endpoints for managing users and their assignments to projects")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Register a new user", description = "Registers a new user with the provided details")
    @PostMapping("auth/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        return userService.register(registerRequest);
    }

    @Operation(summary = "Verify email", description = "Activates user account via email verification token")
    @GetMapping("auth/verify")
    @ResponseStatus(HttpStatus.OK)
    public void verifyEmail(@RequestParam String token) {
        userService.verify(token);
    }

    @Operation(summary = "Login a user", description = "Authenticates a user with the provided email and password")
    @PostMapping("auth/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        return userService.login(loginRequest);
    }

    @Operation(summary = "Assign a user to a project", description = "Assigns the specified user to the specified project")
    @PatchMapping("/projects/{projectId}/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse assignUserToProject(@PathVariable Long userId, @PathVariable Long projectId) {
        return userService.assignUserToProject(userId, projectId);
    }
}
