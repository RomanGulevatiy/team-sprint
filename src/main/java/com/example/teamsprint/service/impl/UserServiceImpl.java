package com.example.teamsprint.service.impl;

import com.example.teamsprint.dto.AuthResponse;
import com.example.teamsprint.dto.LoginRequest;
import com.example.teamsprint.dto.RegisterRequest;
import com.example.teamsprint.dto.UserResponse;
import com.example.teamsprint.entity.Project;
import com.example.teamsprint.entity.User;
import com.example.teamsprint.entity.VerificationToken;
import com.example.teamsprint.entity.enums.UserRole;
import com.example.teamsprint.exception.*;
import com.example.teamsprint.repository.ProjectRepository;
import com.example.teamsprint.repository.UserRepository;
import com.example.teamsprint.repository.VerificationTokenRepository;
import com.example.teamsprint.security.JwtService;
import com.example.teamsprint.security.UserPrincipal;
import com.example.teamsprint.service.EmailService;
import com.example.teamsprint.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Transactional
    @Override
    public AuthResponse register(RegisterRequest registerRequest) {
        if(userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            log.warn("Attempted to register with already used email: {}", registerRequest.getEmail());
            throw new EmailAlreadyExistsException("Email already in use: " + registerRequest.getEmail());
        }

        User user = mapToUserEntity(registerRequest);
        User savedUser = userRepository.save(user);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(savedUser)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
        verificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getUsername(), token);
        log.info("Registered new user with ID: {}. Verification email sent.", savedUser.getId());

        return AuthResponse.builder()
                .user(mapToUserResponse(savedUser))
                .token(null)
                .build();
    }

    @Transactional
    @Override
    public void verify(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Invalid verification token."));

        if(verificationToken.isExpired()) {
            throw new InvalidTokenException("Verification token has expired.");
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        verificationTokenRepository.delete(verificationToken);
        log.info("Email verified for user ID: {}", user.getId());
    }

    @Transactional
    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(
                () -> new EntityNotFoundException("User not found with email: " + loginRequest.getEmail()));

        if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Invalid password");
        }
        if(!user.isEnabled()) {
            throw new AccountNotVerifiedException("Please verify your email before logging in.");
        }

        return mapToAuthResponse(user);
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

    /**
     * Helper method to map User entity to AuthResponse DTO
     *
     * @param user the User entity to be mapped
     * @return the corresponding AuthResponse DTO
     */
    private AuthResponse mapToAuthResponse(User user) {
        UserPrincipal userPrincipal = new UserPrincipal(user);
        String token = jwtService.generateToken(userPrincipal);

        return AuthResponse.builder()
                .user(mapToUserResponse(user))
                .token(token)
                .build();
    }
}
