package com.example.teamsprint.service;

import com.example.teamsprint.dto.response.AuthResponse;
import com.example.teamsprint.dto.request.LoginRequest;
import com.example.teamsprint.dto.request.RegisterRequest;
import com.example.teamsprint.dto.request.RefreshTokenRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest registerRequest);

    void verify(String token);

    AuthResponse login(LoginRequest loginRequest);

    AuthResponse refresh(RefreshTokenRequest refreshTokenRequest);
}
