package com.example.teamsprint.service;

import com.example.teamsprint.dto.UserResponse;

public interface UserService {

    UserResponse assignUserToProject(Long userId, Long projectId);
}
