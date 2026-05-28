package com.example.teamsprint.controller;

import com.example.teamsprint.dto.AuthResponse;
import com.example.teamsprint.dto.LoginRequest;
import com.example.teamsprint.dto.RegisterRequest;
import com.example.teamsprint.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "Endpoints for registering, verifying, and logging in users")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new user", description = "Registers a new user with the provided details")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        return authService.register(registerRequest);
    }

    @Operation(summary = "Verify email", description = "Activates user account via email verification token")
    @GetMapping("/verify")
    @ResponseStatus(HttpStatus.OK)
    public void verifyEmail(@RequestParam String token) {
        authService.verify(token);
    }

    @Operation(summary = "Login a user", description = "Authenticates a user with the provided email and password")
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponse loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }
}

