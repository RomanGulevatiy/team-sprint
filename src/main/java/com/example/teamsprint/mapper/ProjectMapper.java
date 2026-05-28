package com.example.teamsprint.mapper;

import com.example.teamsprint.dto.ProjectRequest;
import com.example.teamsprint.dto.ProjectResponse;
import com.example.teamsprint.entity.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    public Project toEntity(ProjectRequest projectRequest) {
        return Project.builder()
                .title(projectRequest.getTitle())
                .description(projectRequest.getDescription())
                .status(projectRequest.getStatus())
                .build();
    }

    public ProjectResponse toResponse(Project project) {
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
