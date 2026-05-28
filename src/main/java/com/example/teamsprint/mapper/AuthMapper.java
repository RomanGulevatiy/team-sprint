package com.example.teamsprint.mapper;

import com.example.teamsprint.dto.AuthResponse;
import com.example.teamsprint.entity.User;
import com.example.teamsprint.security.JwtService;
import com.example.teamsprint.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthMapper {

    private final JwtService jwtService;
    private final UserMapper userMapper;

    public AuthResponse toAuthResponse(User user) {
        UserPrincipal userPrincipal = new UserPrincipal(user);
        String accessToken = jwtService.generateAccessToken(userPrincipal);
        String refreshToken = jwtService.generateRefreshToken(userPrincipal);

        return AuthResponse.builder()
                .user(userMapper.toResponse(user))
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthResponse toRegistrationResponse(User user) {
        return AuthResponse.builder()
                .user(userMapper.toResponse(user))
                .accessToken(null)
                .refreshToken(null)
                .build();
    }
}
