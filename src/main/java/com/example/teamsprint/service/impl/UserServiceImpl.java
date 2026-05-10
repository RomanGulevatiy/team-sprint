package com.example.teamsprint.service.impl;

import com.example.teamsprint.dto.RegisterRequest;
import com.example.teamsprint.dto.UserResponse;
import com.example.teamsprint.entity.Project;
import com.example.teamsprint.entity.User;
import com.example.teamsprint.entity.enums.UserRole;
import com.example.teamsprint.exception.EntityNotFoundException;
import com.example.teamsprint.repository.ProjectRepository;
import com.example.teamsprint.repository.UserRepository;
import com.example.teamsprint.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public UserResponse register(RegisterRequest registerRequest) {
        User user = mapToUserEntity(registerRequest);

        User savedUser = userRepository.save(user);
        log.info("Registered new user with ID: {}", savedUser.getId());
        return mapToUserResponse(savedUser);
    }

    @Transactional
    @Override
    public UserResponse assignUserToProject(Long userId, Long projectId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("User not found with ID: " + userId));

        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new EntityNotFoundException("Project not found with ID: " + projectId));

        user.getProjects().add(project);
        User updatedUser = userRepository.save(user);
        log.info("Assigned user ID: {} to project ID: {}", userId, projectId);
        return mapToUserResponse(updatedUser);
    }

    /**
     * Helper method to map User entity to UserResponse DTO
     *
     * @param user the User entity to be mapped
     * @return the corresponding UserResponse DTO
     */
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * Helper method to map RegisterRequest DTO to User entity
     *
     * @param registerRequest the RegisterRequest DTO to be mapped
     * @return the corresponding User entity
     */
    private User mapToUserEntity(RegisterRequest registerRequest) {
        return User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(UserRole.USER)
                .build();
    }
}
