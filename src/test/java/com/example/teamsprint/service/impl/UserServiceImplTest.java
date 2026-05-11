package com.example.teamsprint.service.impl;

import com.example.teamsprint.dto.AuthResponse;
import com.example.teamsprint.dto.LoginRequest;
import com.example.teamsprint.dto.RegisterRequest;
import com.example.teamsprint.dto.UserResponse;
import com.example.teamsprint.entity.Project;
import com.example.teamsprint.entity.User;
import com.example.teamsprint.exception.EntityNotFoundException;
import com.example.teamsprint.exception.InvalidPasswordException;
import com.example.teamsprint.repository.ProjectRepository;
import com.example.teamsprint.repository.UserRepository;
import com.example.teamsprint.security.JwtService;
import com.example.teamsprint.security.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("register should encode password, save user and return auth response")
    void register_savesUserAndReturnsResponse() {
        RegisterRequest request = new RegisterRequest("testuser", "test@mail.com", "pass");

        String encodedPassword = "encoded_password_123";
        when(passwordEncoder.encode(request.getPassword())).thenReturn(encodedPassword);

        User savedUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@mail.com")
                .password(encodedPassword)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(UserPrincipal.class))).thenReturn("jwt-token");

        AuthResponse result = userService.register(request);

        assertThat(result.getUser().getUsername()).isEqualTo("testuser");
        assertThat(result.getUser().getEmail()).isEqualTo("test@mail.com");
        assertThat(result.getToken()).isEqualTo("jwt-token");
        verify(passwordEncoder).encode("pass");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("login should return auth response when credentials are valid")
    void login_returnsAuthResponse() {
        LoginRequest request = new LoginRequest("test@mail.com", "pass");
        User user = User.builder()
                .id(2L)
                .username("testuser")
                .email("test@mail.com")
                .password("encoded_pass")
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(any(UserPrincipal.class))).thenReturn("jwt-token");

        AuthResponse result = userService.login(request);

        assertThat(result.getUser().getId()).isEqualTo(2L);
        assertThat(result.getUser().getEmail()).isEqualTo("test@mail.com");
        assertThat(result.getToken()).isEqualTo("jwt-token");
    }

    @Test
    @DisplayName("login should throw InvalidPasswordException when password is wrong")
    void login_throwsException_whenPasswordInvalid() {
        LoginRequest request = new LoginRequest("test@mail.com", "wrong");
        User user = User.builder()
                .id(2L)
                .email("test@mail.com")
                .password("encoded_pass")
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(InvalidPasswordException.class)
                .hasMessageContaining("Invalid password");
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