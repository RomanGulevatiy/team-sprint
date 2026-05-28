package com.example.teamsprint.service.impl;

import com.example.teamsprint.dto.AuthResponse;
import com.example.teamsprint.dto.LoginRequest;
import com.example.teamsprint.dto.RegisterRequest;
import com.example.teamsprint.entity.User;
import com.example.teamsprint.entity.VerificationToken;
import com.example.teamsprint.exception.AccountNotVerifiedException;
import com.example.teamsprint.exception.EmailAlreadyExistsException;
import com.example.teamsprint.exception.EntityNotFoundException;
import com.example.teamsprint.exception.InvalidPasswordException;
import com.example.teamsprint.exception.InvalidTokenException;
import com.example.teamsprint.mapper.AuthMapper;
import com.example.teamsprint.mapper.UserMapper;
import com.example.teamsprint.repository.UserRepository;
import com.example.teamsprint.repository.VerificationTokenRepository;
import com.example.teamsprint.service.AuthService;
import com.example.teamsprint.service.EmailService;
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
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UserMapper userMapper;
    private final AuthMapper authMapper;

    @Transactional
    @Override
    public AuthResponse register(RegisterRequest registerRequest) {
        if(userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            log.warn("Attempted to register with already used email: {}", registerRequest.getEmail());
            throw new EmailAlreadyExistsException("Email already in use: " + registerRequest.getEmail());
        }

        String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());
        User user = userMapper.toEntity(registerRequest, encodedPassword);
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

        return authMapper.toRegistrationResponse(savedUser);
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

        return authMapper.toAuthResponse(user);
    }
}
