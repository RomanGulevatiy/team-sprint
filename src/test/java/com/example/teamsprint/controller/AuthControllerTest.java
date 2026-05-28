package com.example.teamsprint.controller;

import com.example.teamsprint.dto.response.AuthResponse;
import com.example.teamsprint.dto.request.LoginRequest;
import com.example.teamsprint.dto.request.RefreshTokenRequest;
import com.example.teamsprint.dto.request.RegisterRequest;
import com.example.teamsprint.dto.response.UserResponse;
import com.example.teamsprint.security.JwtService;
import com.example.teamsprint.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/auth/register should return 201 Created with token and user")
    void registerUser_returnsCreated() throws Exception {
        RegisterRequest request = new RegisterRequest("john", "john@mail.com", "secret");
        UserResponse user = UserResponse.builder()
                .id(1L)
                .username("john")
                .email("john@mail.com")
                .build();
        AuthResponse response = AuthResponse.builder()
                .accessToken("token-123")
                .refreshToken(null)
                .user(user)
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("token-123"))
                .andExpect(jsonPath("$.user.id").value(1L))
                .andExpect(jsonPath("$.user.username").value("john"))
                .andExpect(jsonPath("$.user.email").value("john@mail.com"));
    }

    @Test
    @DisplayName("POST /api/auth/login should return 200 OK with token and user")
    void loginUser_returnsOk() throws Exception {
        LoginRequest request = new LoginRequest("john@mail.com", "secret");
        UserResponse user = UserResponse.builder()
                .id(1L)
                .username("john")
                .email("john@mail.com")
                .build();
        AuthResponse response = AuthResponse.builder()
                .accessToken("token-456")
                .refreshToken("refresh-456")
                .user(user)
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("token-456"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-456"))
                .andExpect(jsonPath("$.user.id").value(1L))
                .andExpect(jsonPath("$.user.username").value("john"))
                .andExpect(jsonPath("$.user.email").value("john@mail.com"));
    }

    @Test
    @DisplayName("POST /api/auth/refresh should return 200 OK with new tokens and user")
    void refreshToken_returnsOk() throws Exception {
        RefreshTokenRequest request = RefreshTokenRequest.builder()
                .refreshToken("refresh-abc")
                .build();
        UserResponse user = UserResponse.builder()
                .id(1L)
                .username("john")
                .email("john@mail.com")
                .build();
        AuthResponse response = AuthResponse.builder()
                .accessToken("token-789")
                .refreshToken("refresh-789")
                .user(user)
                .build();

        when(authService.refresh(any(RefreshTokenRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("token-789"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-789"))
                .andExpect(jsonPath("$.user.id").value(1L))
                .andExpect(jsonPath("$.user.username").value("john"))
                .andExpect(jsonPath("$.user.email").value("john@mail.com"));
    }
}


