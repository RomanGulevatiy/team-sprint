package com.example.teamsprint.service;

import com.example.teamsprint.dto.response.PageResponse;
import com.example.teamsprint.dto.request.ProjectRequest;
import com.example.teamsprint.dto.response.ProjectResponse;
import com.example.teamsprint.entity.enums.ProjectStatus;

public interface ProjectService {

    PageResponse<ProjectResponse> getProjectsForUser(Long userId,
                                                     ProjectStatus status,
                                                     String title,
                                                     int page,
                                                     int size);

    ProjectResponse createProject(ProjectRequest projectRequest, Long userId);

    void deleteProject(Long projectId);
}
