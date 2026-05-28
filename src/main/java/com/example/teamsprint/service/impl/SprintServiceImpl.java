package com.example.teamsprint.service.impl;

import com.example.teamsprint.dto.PageResponse;
import com.example.teamsprint.dto.SprintRequest;
import com.example.teamsprint.dto.SprintResponse;
import com.example.teamsprint.entity.Project;
import com.example.teamsprint.entity.Sprint;
import com.example.teamsprint.entity.enums.SprintStatus;
import com.example.teamsprint.exception.EntityNotFoundException;
import com.example.teamsprint.exception.UserNotInProjectException;
import com.example.teamsprint.mapper.SprintMapper;
import com.example.teamsprint.repository.ProjectRepository;
import com.example.teamsprint.repository.SprintRepository;
import com.example.teamsprint.service.SprintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SprintServiceImpl implements SprintService {

    private final ProjectRepository projectRepository;
    private final SprintRepository sprintRepository;
    private final SprintMapper sprintMapper;

    @Transactional
    @Override
    public SprintResponse createSprint(Long projectId, SprintRequest sprintRequest, Long userId) {

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with ID: " + projectId));

        if(!projectRepository.existsByIdAndUsers_Id(projectId, userId)) {
            throw new UserNotInProjectException("User with ID: " + userId + " is not part of project ID: " + projectId);
        }

        Sprint sprint = sprintMapper.toEntity(sprintRequest);
        sprint.setProject(project);

        Sprint savedSprint = sprintRepository.save(sprint);
        log.info("Created new sprint with ID: {}", savedSprint.getId());
        return sprintMapper.toResponse(savedSprint);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<SprintResponse> getSprintsByProjectId(Long projectId,
                                                              SprintStatus status,
                                                              int page,
                                                              int size,
                                                              Long userId) {
        if(!projectRepository.existsById(projectId)) {
            throw new EntityNotFoundException("Project not found with ID: " + projectId);
        }
        if(!projectRepository.existsByIdAndUsers_Id(projectId, userId)) {
            throw new UserNotInProjectException("User with ID: " + userId + " is not part of project ID: " + projectId);
        }

        Specification<Sprint> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("project").get("id"), projectId));

            if(status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Sprint> sprintPage = sprintRepository.findAll(spec, PageRequest.of(page, size));
        List<SprintResponse> content = sprintPage.getContent().stream()
                .map(sprintMapper::toResponse)
                .toList();

        log.info("Retrieved {} sprints for project ID: {}", sprintPage.getNumberOfElements(), projectId);
        return PageResponse.<SprintResponse>builder()
                .content(content)
                .pageNumber(sprintPage.getNumber())
                .pageSize(sprintPage.getSize())
                .totalElements(sprintPage.getTotalElements())
                .totalPages(sprintPage.getTotalPages())
                .build();
    }

}
