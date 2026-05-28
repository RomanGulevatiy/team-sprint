package com.example.teamsprint.service;

import com.example.teamsprint.dto.request.UpdateSprintRequest;
import com.example.teamsprint.dto.response.PageResponse;
import com.example.teamsprint.dto.request.SprintRequest;
import com.example.teamsprint.dto.response.SprintResponse;
import com.example.teamsprint.entity.enums.SprintStatus;

public interface SprintService {

    SprintResponse createSprint(Long projectId, SprintRequest sprintRequest, Long userId);

    PageResponse<SprintResponse> getSprintsByProjectId(Long projectId,
                                                      SprintStatus status,
                                                      int page,
                                                      int size,
                                                      Long userId);

    SprintResponse updateSprint(Long sprintId, UpdateSprintRequest request, Long userId);

    void deleteSprint(Long sprintId, Long userId);
}
