package com.example.teamsprint.controller;

import com.example.teamsprint.dto.request.UpdateSprintRequest;
import com.example.teamsprint.dto.response.PageResponse;
import com.example.teamsprint.dto.request.SprintRequest;
import com.example.teamsprint.dto.response.SprintResponse;
import com.example.teamsprint.entity.enums.SprintStatus;
import com.example.teamsprint.security.UserPrincipal;
import com.example.teamsprint.service.SprintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Sprint Management", description = "Endpoints for managing sprints within projects")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SprintController {

    private final SprintService sprintService;

    @Operation(summary = "Create a new sprint for a project", description = "Creates a new sprint under the specified project")
    @PostMapping("projects/{projectId}/sprints")
    @ResponseStatus(HttpStatus.CREATED)
    public SprintResponse createSprint(@PathVariable Long projectId,
                                       @AuthenticationPrincipal UserPrincipal principal,
                                       @Valid @RequestBody SprintRequest sprintRequest) {
        return sprintService.createSprint(projectId, sprintRequest, principal.getId());
    }

    @Operation(summary = "Get all sprints for a project", description = "Returns a paginated list of sprints with optional filters")
    @GetMapping("projects/{projectId}/sprints")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<SprintResponse> getSprintsByProjectId(@PathVariable Long projectId,
                                                              @AuthenticationPrincipal UserPrincipal principal,
                                                              @RequestParam(required = false) SprintStatus status,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size) {
        return sprintService.getSprintsByProjectId(projectId,
                status,
                page,
                size,
                principal.getId());
    }

    @Operation(summary = "Update a sprint", description = "Updates the details of an existing sprint")
    @PatchMapping("sprints/{sprintId}")
    @ResponseStatus(HttpStatus.OK)
    public SprintResponse updateSprint(@PathVariable Long sprintId,
                                       @AuthenticationPrincipal UserPrincipal principal,
                                       @Valid @RequestBody UpdateSprintRequest request) {
        return sprintService.updateSprint(sprintId, request, principal.getId());
    }

    @Operation(summary = "Delete a sprint", description = "Deletes an existing sprint by its ID")
    @DeleteMapping("sprints/{sprintId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSprint(@PathVariable Long sprintId,
                             @AuthenticationPrincipal UserPrincipal principal) {
        sprintService.deleteSprint(sprintId, principal.getId());
    }
}
