package com.example.teamsprint.service.impl;

import com.example.teamsprint.dto.SprintRequest;
import com.example.teamsprint.dto.SprintResponse;
import com.example.teamsprint.entity.Project;
import com.example.teamsprint.entity.Sprint;
import com.example.teamsprint.exception.EntityNotFoundException;
import com.example.teamsprint.repository.ProjectRepository;
import com.example.teamsprint.repository.SprintRepository;
import com.example.teamsprint.service.SprintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SprintServiceImpl implements SprintService {

    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;

    @Transactional
    @Override
    public SprintResponse createSprint(Long projectId, SprintRequest sprintRequest) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with ID: " + projectId));

        Sprint sprint = mapToSprintEntity(sprintRequest);
        sprint.setProject(project);

        Sprint savedSprint = sprintRepository.save(sprint);
        log.info("Created new sprint with ID: {}", savedSprint.getId());
        return mapToSprintResponse(savedSprint);
    }

    @Transactional(readOnly = true)
    @Override
    public List<SprintResponse> getSprintsByProjectId(Long projectId) {

        if(!projectRepository.existsById(projectId)) {
            throw new EntityNotFoundException("Project not found with ID: " + projectId);
        }
        List<Sprint> sprints = sprintRepository.findByProjectId(projectId);
        log.info("Retrieved {} sprints for project ID: {}", sprints.size(), projectId);

        return sprints.stream()
                .map(this::mapToSprintResponse)
                .toList();
    }

    /**
     * Helper method to map SprintRequest DTO to Sprint entity
     *
     * @param sprintRequest the SprintRequest DTO to be mapped
     * @return the corresponding Sprint entity
     */
    private Sprint mapToSprintEntity(SprintRequest sprintRequest) {
        return Sprint.builder()
                .title(sprintRequest.getTitle())
                .description(sprintRequest.getDescription())
                .status(sprintRequest.getStatus())
                .startDate(sprintRequest.getStartDate())
                .dueDate(sprintRequest.getDueDate())
                .build();
    }

    /**
     * Helper method to map Sprint entity to SprintResponse DTO
     *
     * @param sprint the Sprint entity to be mapped
     * @return the corresponding SprintResponse DTO
     */
    private SprintResponse mapToSprintResponse(Sprint  sprint) {
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
