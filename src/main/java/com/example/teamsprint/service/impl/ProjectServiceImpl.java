package com.example.teamsprint.service.impl;

import com.example.teamsprint.dto.ProjectRequest;
import com.example.teamsprint.dto.ProjectResponse;
import com.example.teamsprint.entity.Project;
import com.example.teamsprint.repository.ProjectRepository;
import com.example.teamsprint.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    @Override
    public List<ProjectResponse> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::mapToProjectResponse)
                .toList();
    }

    @Transactional
    @Override
    public ProjectResponse createProject(ProjectRequest projectRequest) {
        Project project = mapToProjectEntity(projectRequest);

        Project savedProject = projectRepository.save(project);
        log.info("Created new project with ID: {}", savedProject.getId());
        return mapToProjectResponse(savedProject);
    }

    /**
     * Helper method to map ProjectRequest DTO to Project entity
     *
     * @param projectRequest the ProjectRequest DTO to be mapped
     * @return the corresponding Project entity
     */
    private Project mapToProjectEntity(ProjectRequest projectRequest) {
        return Project.builder()
                .title(projectRequest.getTitle())
                .description(projectRequest.getDescription())
                .status(projectRequest.getStatus())
                .build();
    }

    /**
     * Helper method to map Project entity to ProjectResponse DTO
     *
     * @param project the Project entity to be mapped
     * @return the corresponding ProjectResponse DTO
     */
    private ProjectResponse mapToProjectResponse(Project project) {
        return ProjectResponse.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .status(project.getStatus())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}
