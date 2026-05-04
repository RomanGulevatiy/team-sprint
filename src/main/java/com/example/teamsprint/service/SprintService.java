package com.example.teamsprint.service;

import com.example.teamsprint.dto.SprintRequest;
import com.example.teamsprint.dto.SprintResponse;

public interface SprintService {

    SprintResponse createSprint(Long projectId, SprintRequest sprintRequest);
}
