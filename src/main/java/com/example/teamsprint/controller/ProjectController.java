package com.example.teamsprint.controller;

import com.example.teamsprint.dto.PageResponse;
import com.example.teamsprint.dto.ProjectRequest;
import com.example.teamsprint.dto.ProjectResponse;
import com.example.teamsprint.entity.enums.ProjectStatus;
import com.example.teamsprint.security.UserPrincipal;
import com.example.teamsprint.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Project Management", description = "Endpoints for managing projects")
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @Operation(summary = "Get all projects with pagination", description = "Returns a paginated list of projects with optional filters")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<ProjectResponse> getProjectsForUser(@AuthenticationPrincipal UserPrincipal principal,
                                                            @RequestParam(required = false) ProjectStatus status,
                                                            @RequestParam(required = false) String title,
                                                            @RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "10") int size) {
        return projectService.getProjectsForUser(principal.getId(), status, title, page, size);
    }

    @Operation(summary = "Create a new project", description = "Creates a new project with the provided details")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse createProject(@AuthenticationPrincipal UserPrincipal principal,
                                         @Valid @RequestBody ProjectRequest projectRequest) {
        return projectService.createProject(projectRequest, principal.getId());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a project", description = "Deletes the project with the specified ID")
    @DeleteMapping("/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
    }
}
