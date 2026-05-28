package com.example.teamsprint.controller;

import com.example.teamsprint.dto.response.UserResponse;
import com.example.teamsprint.security.UserPrincipal;
import com.example.teamsprint.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Management", description = "Endpoints for managing users and their assignments to projects")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Assign a user to a project", description = "Assigns the specified user to the specified project")
    @PatchMapping("/projects/{projectId}/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public UserResponse assignUserToProject(@PathVariable Long userId,
                                            @PathVariable Long projectId,
                                            @AuthenticationPrincipal UserPrincipal principal) {
        return userService.assignUserToProject(userId, projectId, principal.getId());
    }
}
