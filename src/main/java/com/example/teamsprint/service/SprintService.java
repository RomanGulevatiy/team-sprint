package com.example.teamsprint.service;

import com.example.teamsprint.dto.PageResponse;
import com.example.teamsprint.dto.SprintRequest;
import com.example.teamsprint.dto.SprintResponse;
import com.example.teamsprint.entity.enums.SprintStatus;

public interface SprintService {

    SprintResponse createSprint(Long projectId, SprintRequest sprintRequest);

    PageResponse<SprintResponse> getSprintsByProjectId(Long projectId,
                                                       SprintStatus status,
                                                       int page,
                                                       int size);
}
