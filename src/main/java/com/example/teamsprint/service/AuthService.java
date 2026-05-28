package com.example.teamsprint.service;

import com.example.teamsprint.dto.AuthResponse;
import com.example.teamsprint.dto.LoginRequest;
import com.example.teamsprint.dto.RegisterRequest;
import com.example.teamsprint.dto.RefreshTokenRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest registerRequest);

    void verify(String token);

    AuthResponse login(LoginRequest loginRequest);

    AuthResponse refresh(RefreshTokenRequest refreshTokenRequest);
}
