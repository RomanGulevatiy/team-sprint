package com.example.teamsprint.service;

import com.example.teamsprint.dto.response.UserResponse;

public interface UserService {

    UserResponse assignUserToProject(Long userId, Long projectId, Long requesterId);
}
