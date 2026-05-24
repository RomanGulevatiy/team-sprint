package com.example.teamsprint.service;

import com.example.teamsprint.dto.PageResponse;
import com.example.teamsprint.dto.ProjectRequest;
import com.example.teamsprint.dto.ProjectResponse;

public interface ProjectService {

    PageResponse<ProjectResponse> getAllProjects(int page, int size);

    PageResponse<ProjectResponse> getProjectsForUser(Long userId, int page, int size);

    ProjectResponse createProject(ProjectRequest projectRequest, Long userId);

    void deleteProject(Long projectId);
}
