package com.example.teamsprint.controller;

import com.example.teamsprint.dto.SprintRequest;
import com.example.teamsprint.dto.SprintResponse;
import com.example.teamsprint.service.SprintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
                                       @Valid @RequestBody SprintRequest sprintRequest) {
        return sprintService.createSprint(projectId, sprintRequest);
    }

    @Operation(summary = "Get all sprints for a project", description = "Returns a list of all sprints associated with the specified project")
    @GetMapping("projects/{projectId}/sprints")
    @ResponseStatus(HttpStatus.OK)
    public List<SprintResponse> getSprintsByProjectId(@PathVariable Long projectId) {
        return sprintService.getSprintsByProjectId(projectId);
    }
}
