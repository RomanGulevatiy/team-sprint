package com.example.teamsprint.mapper;

import com.example.teamsprint.dto.RegisterRequest;
import com.example.teamsprint.dto.UserResponse;
import com.example.teamsprint.entity.User;
import com.example.teamsprint.entity.enums.UserRole;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(RegisterRequest registerRequest, String encodedPassword) {
        return User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(encodedPassword)
                .role(UserRole.USER)
                .build();
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
