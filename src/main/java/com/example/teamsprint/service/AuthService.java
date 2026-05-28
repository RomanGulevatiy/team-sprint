package com.example.teamsprint.service;

import com.example.teamsprint.dto.AuthResponse;
import com.example.teamsprint.dto.LoginRequest;
import com.example.teamsprint.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest registerRequest);

    void verify(String token);

    AuthResponse login(LoginRequest loginRequest);
}
