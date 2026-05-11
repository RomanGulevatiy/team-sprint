package com.example.teamsprint.controller;

import com.example.teamsprint.dto.AuthResponse;
import com.example.teamsprint.dto.LoginRequest;
import com.example.teamsprint.dto.RegisterRequest;
import com.example.teamsprint.dto.UserResponse;
import com.example.teamsprint.security.JwtService;
import com.example.teamsprint.service.UserService;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

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
                .token("token-123")
                .user(user)
                .build();

        when(userService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("token-123"))
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
                .token("token-456")
                .user(user)
                .build();

        when(userService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-456"))
                .andExpect(jsonPath("$.user.id").value(1L))
                .andExpect(jsonPath("$.user.username").value("john"))
                .andExpect(jsonPath("$.user.email").value("john@mail.com"));
    }

    @Test
    @DisplayName("PATCH /api/projects/{projectId}/users/{userId} should return 200 OK")
    void assignUserToProject_returnsOk() throws Exception {
        UserResponse response = UserResponse.builder().id(1L).username("john").build();

        when(userService.assignUserToProject(anyLong(), anyLong())).thenReturn(response);

        mockMvc.perform(patch("/api/projects/10/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("john"));
    }
}