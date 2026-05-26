package com.example.teamsprint.service.impl;

import com.example.teamsprint.dto.AuthResponse;
import com.example.teamsprint.dto.LoginRequest;
import com.example.teamsprint.dto.RegisterRequest;
import com.example.teamsprint.dto.UserResponse;
import com.example.teamsprint.entity.Project;
import com.example.teamsprint.entity.User;
import com.example.teamsprint.entity.VerificationToken;
import com.example.teamsprint.exception.AccountNotVerifiedException;
import com.example.teamsprint.exception.EmailAlreadyExistsException;
import com.example.teamsprint.exception.InvalidTokenException;
import com.example.teamsprint.exception.EntityNotFoundException;
import com.example.teamsprint.repository.ProjectRepository;
import com.example.teamsprint.repository.UserRepository;
import com.example.teamsprint.repository.VerificationTokenRepository;
import com.example.teamsprint.service.EmailService;
import com.example.teamsprint.security.JwtService;
import com.example.teamsprint.security.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("register should encode password, save user, create token and send verification email")
    void register_savesUserAndSendsVerificationEmail() {
        RegisterRequest request = new RegisterRequest("testuser", "test@mail.com", "pass123");

        String encodedPassword = "encoded_password_123";
        when(passwordEncoder.encode(request.getPassword())).thenReturn(encodedPassword);
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        User savedUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@mail.com")
                .password(encodedPassword)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        AuthResponse result = userService.register(request);

        assertThat(result.getUser().getUsername()).isEqualTo("testuser");
        assertThat(result.getUser().getEmail()).isEqualTo("test@mail.com");
        assertThat(result.getToken()).isNull();
        verify(passwordEncoder).encode("pass123");
        verify(userRepository).save(any(User.class));
        verify(verificationTokenRepository).save(any(VerificationToken.class));
        verify(emailService).sendVerificationEmail(eq("test@mail.com"), eq("testuser"), any(String.class));
    }

    @Test
    @DisplayName("register should throw EmailAlreadyExistsException when email is already used")
    void register_throwsException_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("testuser", "test@mail.com", "pass123");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("Email already in use: test@mail.com");
    }

    @Test
    @DisplayName("verify should enable user and delete token when token is valid")
    void verify_enablesUserAndDeletesToken() {
        User user = User.builder().id(3L).enabled(false).build();
        VerificationToken token = VerificationToken.builder()
                .token("valid-token")
                .user(user)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();

        when(verificationTokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));

        userService.verify("valid-token");

        assertThat(user.isEnabled()).isTrue();
        verify(userRepository).save(user);
        verify(verificationTokenRepository).delete(token);
    }

    @Test
    @DisplayName("verify should throw InvalidTokenException when token is not found")
    void verify_throwsException_whenTokenNotFound() {
        when(verificationTokenRepository.findByToken("missing-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.verify("missing-token"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid verification token");
    }

    @Test
    @DisplayName("verify should throw InvalidTokenException when token is expired")
    void verify_throwsException_whenTokenExpired() {
        User user = User.builder().id(3L).enabled(false).build();
        VerificationToken token = VerificationToken.builder()
                .token("expired-token")
                .user(user)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .build();

        when(verificationTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> userService.verify("expired-token"))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Verification token has expired");
    }

    @Test
    @DisplayName("login should return auth response when credentials are valid and account verified")
    void login_returnsAuthResponse() {
        LoginRequest request = new LoginRequest("test@mail.com", "pass");
        User user = User.builder()
                .id(2L)
                .username("testuser")
                .email("test@mail.com")
                .password("encoded_pass")
                .enabled(true)
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
    @DisplayName("login should throw AccountNotVerifiedException when account is not verified")
    void login_throwsException_whenAccountNotVerified() {
        LoginRequest request = new LoginRequest("test@mail.com", "pass");
        User user = User.builder()
                .id(2L)
                .username("testuser")
                .email("test@mail.com")
                .password("encoded_pass")
                .enabled(false)
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(AccountNotVerifiedException.class)
                .hasMessageContaining("Please verify your email before logging in");
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