package com.example.teamsprint.controller;

import com.example.teamsprint.dto.response.UserResponse;
import com.example.teamsprint.entity.User;
import com.example.teamsprint.entity.enums.UserRole;
import com.example.teamsprint.security.UserPrincipal;
import com.example.teamsprint.service.UserService;
import com.example.teamsprint.security.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

    @BeforeEach
    void setUpSecurityContext() {
        UserPrincipal principal = new UserPrincipal(User.builder()
                .id(1L)
                .email("user@example.com")
                .password("secret")
                .role(UserRole.USER)
                .build());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("PATCH /api/projects/{projectId}/users/{userId} should return 200 OK")
    void assignUserToProject_returnsOk() throws Exception {
        UserResponse response = UserResponse.builder().id(1L).username("john").build();
        when(userService.assignUserToProject(anyLong(), anyLong(), eq(1L))).thenReturn(response);

        mockMvc.perform(patch("/api/projects/10/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("john"));
    }
}