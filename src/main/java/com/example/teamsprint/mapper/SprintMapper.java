package com.example.teamsprint.mapper;

import com.example.teamsprint.dto.SprintRequest;
import com.example.teamsprint.dto.SprintResponse;
import com.example.teamsprint.entity.Sprint;
import org.springframework.stereotype.Component;

@Component
public class SprintMapper {

    public Sprint toEntity(SprintRequest sprintRequest) {
        return Sprint.builder()
                .title(sprintRequest.getTitle())
                .description(sprintRequest.getDescription())
                .status(sprintRequest.getStatus())
                .startDate(sprintRequest.getStartDate())
                .dueDate(sprintRequest.getDueDate())
                .build();
    }

    public SprintResponse toResponse(Sprint sprint) {
        return SprintResponse.builder()
                .id(sprint.getId())
                .title(sprint.getTitle())
                .description(sprint.getDescription())
                .status(sprint.getStatus())
                .startDate(sprint.getStartDate())
                .dueDate(sprint.getDueDate())
                .projectId(sprint.getProject().getId())
                .createdAt(sprint.getCreatedAt())
                .updatedAt(sprint.getUpdatedAt())
                .build();
    }
}
