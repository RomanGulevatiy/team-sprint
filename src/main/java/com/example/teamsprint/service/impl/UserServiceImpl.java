package com.example.teamsprint.service.impl;

import com.example.teamsprint.dto.response.UserResponse;
import com.example.teamsprint.entity.Project;
import com.example.teamsprint.entity.User;
import com.example.teamsprint.exception.EntityNotFoundException;
import com.example.teamsprint.exception.UserAlreadyInProjectException;
import com.example.teamsprint.exception.UserNotInProjectException;
import com.example.teamsprint.mapper.UserMapper;
import com.example.teamsprint.repository.ProjectRepository;
import com.example.teamsprint.repository.UserRepository;
import com.example.teamsprint.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final UserMapper userMapper;

    @Transactional
    @Override
    public UserResponse assignUserToProject(Long userId, Long projectId, Long requesterId) {
        if(!projectRepository.existsByIdAndUsers_Id(projectId, requesterId)) {
            throw new UserNotInProjectException("User with ID: " + requesterId + " is not part of project ID: " + projectId);
        }

        User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("User not found with ID: " + userId));

        Project project = projectRepository.findById(projectId).orElseThrow(
                () -> new EntityNotFoundException("Project not found with ID: " + projectId));

        if(user.getProjects().contains(project)) {
            throw new UserAlreadyInProjectException("User ID: " + userId + " is already assigned to project ID " + projectId);
        }

        user.getProjects().add(project);
        User updatedUser = userRepository.save(user);
        log.info("Assigned user ID: {} to project ID: {}", userId, projectId);
        return userMapper.toResponse(updatedUser);
    }
}
