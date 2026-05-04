package com.example.teamsprint.service;

import com.example.teamsprint.dto.SprintRequest;
import com.example.teamsprint.dto.SprintResponse;

import java.util.List;

public interface SprintService {

    SprintResponse createSprint(Long projectId, SprintRequest sprintRequest);

    List<SprintResponse> getSprintsByProjectId(Long projectId);
}
