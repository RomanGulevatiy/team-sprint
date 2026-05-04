package com.example.teamsprint.controller;

import com.example.teamsprint.dto.SprintRequest;
import com.example.teamsprint.dto.SprintResponse;
import com.example.teamsprint.service.SprintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SprintController {

    private final SprintService sprintService;

    @PostMapping("projects/{projectId}/sprints")
    @ResponseStatus(HttpStatus.CREATED)
    public SprintResponse createSprint(@PathVariable Long projectId,
                                       @Valid @RequestBody SprintRequest sprintRequest) {
        return sprintService.createSprint(projectId, sprintRequest);
    }

    @GetMapping("projects/{projectId}/sprints")
    @ResponseStatus(HttpStatus.OK)
    public List<SprintResponse> getSprintsByProjectId(@PathVariable Long projectId) {
        return sprintService.getSprintsByProjectId(projectId);
    }
}
