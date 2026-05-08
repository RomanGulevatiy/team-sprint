package com.example.teamsprint.service.impl;

import com.example.teamsprint.dto.RegisterRequest;
import com.example.teamsprint.dto.UserResponse;
import com.example.teamsprint.entity.Project;
import com.example.teamsprint.entity.User;
import com.example.teamsprint.exception.EntityNotFoundException;
import com.example.teamsprint.repository.ProjectRepository;
import com.example.teamsprint.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("register should save user and return response")
    void register_savesUserAndReturnsResponse() {
        RegisterRequest request = new RegisterRequest("testuser", "test@mail.com", "pass");
        User savedUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@mail.com")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse result = userService.register(request);

        assertThat(result.getUsername()).isEqualTo("testuser");
        assertThat(result.getId()).isEqualTo(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("assignUserToProject should add project to user when both exist")
    void assignUserToProject_success() {
        Long userId = 1L;
        Long projectId = 10L;

        User user = User.builder().id(userId).projects(new HashSet<>()).build();
        Project project = Project.builder().id(projectId).title("Project X").build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse result = userService.assignUserToProject(userId, projectId);

        assertThat(user.getProjects()).contains(project);
        assertThat(result.getId()).isEqualTo(userId);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("assignUserToProject should throw EntityNotFoundException when user not found")
    void assignUserToProject_throwsException_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.assignUserToProject(1L, 10L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found with ID: 1");
    }

    @Test
    @DisplayName("assignUserToProject should throw EntityNotFoundException when project not found")
    void assignUserToProject_throwsException_whenProjectNotFound() {
        User user = User.builder().id(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(projectRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.assignUserToProject(1L, 10L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Project not found with ID: 10");
    }
}