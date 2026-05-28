package com.example.teamsprint.service.impl;

import com.example.teamsprint.dto.AuthResponse;
import com.example.teamsprint.dto.LoginRequest;
import com.example.teamsprint.dto.RefreshTokenRequest;
import com.example.teamsprint.dto.RegisterRequest;
import com.example.teamsprint.dto.UserResponse;
import com.example.teamsprint.entity.User;
import com.example.teamsprint.entity.VerificationToken;
import com.example.teamsprint.exception.AccountNotVerifiedException;
import com.example.teamsprint.exception.EmailAlreadyExistsException;
import com.example.teamsprint.exception.InvalidTokenException;
import com.example.teamsprint.mapper.AuthMapper;
import com.example.teamsprint.mapper.UserMapper;
import com.example.teamsprint.repository.UserRepository;
import com.example.teamsprint.repository.VerificationTokenRepository;
import com.example.teamsprint.security.JwtService;
import com.example.teamsprint.service.EmailService;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuthMapper authMapper;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("register should encode password, save user, create token and send verification email")
    void register_savesUserAndSendsVerificationEmail() {
        RegisterRequest request = new RegisterRequest("testuser", "test@mail.com", "pass123");
        String encodedPassword = "encoded_password_123";

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn(encodedPassword);
        when(userMapper.toEntity(request, encodedPassword)).thenReturn(User.builder()
                .username("testuser")
                .email("test@mail.com")
                .password(encodedPassword)
                .build());

        User savedUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@mail.com")
                .password(encodedPassword)
                .build();
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        AuthResponse expected = AuthResponse.builder()
                .user(UserResponse.builder().id(1L).username("testuser").email("test@mail.com").build())
                .accessToken(null)
                .refreshToken(null)
                .build();
        when(authMapper.toRegistrationResponse(savedUser)).thenReturn(expected);

        AuthResponse result = authService.register(request);

        assertThat(result.getUser().getUsername()).isEqualTo("testuser");
        assertThat(result.getUser().getEmail()).isEqualTo("test@mail.com");
        assertThat(result.getAccessToken()).isNull();
        verify(passwordEncoder).encode("pass123");
        verify(userRepository).save(any(User.class));
        verify(verificationTokenRepository).save(any(VerificationToken.class));
        verify(emailService).sendVerificationEmail(eq("test@mail.com"), eq("testuser"), anyString());
    }

    @Test
    @DisplayName("register should throw EmailAlreadyExistsException when email is already used")
    void register_throwsException_whenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("testuser", "test@mail.com", "pass123");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authService.register(request))
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

        authService.verify("valid-token");

        assertThat(user.isEnabled()).isTrue();
        verify(userRepository).save(user);
        verify(verificationTokenRepository).delete(token);
    }

    @Test
    @DisplayName("verify should throw InvalidTokenException when token is not found")
    void verify_throwsException_whenTokenNotFound() {
        when(verificationTokenRepository.findByToken("missing-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.verify("missing-token"))
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

        assertThatThrownBy(() -> authService.verify("expired-token"))
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
        AuthResponse response = AuthResponse.builder()
                .user(UserResponse.builder().id(2L).email("test@mail.com").build())
                .accessToken("jwt-token")
                .refreshToken("refresh-token")
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.getPassword(), user.getPassword())).thenReturn(true);
        when(authMapper.toAuthResponse(user)).thenReturn(response);

        AuthResponse result = authService.login(request);

        assertThat(result.getUser().getId()).isEqualTo(2L);
        assertThat(result.getUser().getEmail()).isEqualTo("test@mail.com");
        assertThat(result.getAccessToken()).isEqualTo("jwt-token");
    }

    @Test
    @DisplayName("refresh should return new auth response when refresh token is valid")
    void refresh_returnsAuthResponse() {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("refresh-token")
                .build();
        User user = User.builder()
                .id(5L)
                .username("testuser")
                .email("test@mail.com")
                .enabled(true)
                .build();
        AuthResponse response = AuthResponse.builder()
                .user(UserResponse.builder().id(5L).email("test@mail.com").build())
                .accessToken("new-access")
                .refreshToken("new-refresh")
                .build();

        when(jwtService.extractEmail("refresh-token")).thenReturn("test@mail.com");
        when(userRepository.findByEmail("test@mail.com")).thenReturn(Optional.of(user));
        when(jwtService.validateToken(anyString(), any())).thenReturn(true);
        when(authMapper.toAuthResponse(user)).thenReturn(response);

        AuthResponse result = authService.refresh(request);

        assertThat(result.getAccessToken()).isEqualTo("new-access");
        assertThat(result.getRefreshToken()).isEqualTo("new-refresh");
    }

    @Test
    @DisplayName("refresh should throw InvalidTokenException when refresh token is invalid")
    void refresh_throwsException_whenTokenInvalid() {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("bad-token")
                .build();

        when(jwtService.extractEmail("bad-token")).thenThrow(new JwtException("invalid"));

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessageContaining("Invalid refresh token");
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

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(AccountNotVerifiedException.class)
                .hasMessageContaining("Please verify your email before logging in");
    }
}


