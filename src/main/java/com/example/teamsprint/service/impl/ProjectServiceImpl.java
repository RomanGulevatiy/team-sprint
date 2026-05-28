package com.example.teamsprint.service.impl;

import com.example.teamsprint.dto.response.PageResponse;
import com.example.teamsprint.dto.request.ProjectRequest;
import com.example.teamsprint.dto.response.ProjectResponse;
import com.example.teamsprint.entity.Project;
import com.example.teamsprint.entity.User;
import com.example.teamsprint.entity.enums.ProjectStatus;
import com.example.teamsprint.exception.EntityNotFoundException;
import com.example.teamsprint.mapper.ProjectMapper;
import com.example.teamsprint.repository.ProjectRepository;
import com.example.teamsprint.repository.UserRepository;
import com.example.teamsprint.service.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;


    @Transactional
    @Override
    public ProjectResponse createProject(ProjectRequest projectRequest, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new EntityNotFoundException("User not found with ID: " + userId));

        Project project = projectMapper.toEntity(projectRequest);
        Project savedProject = projectRepository.save(project);

        if(user.getProjects() == null) {
            user.setProjects(new HashSet<>());
        }
        user.getProjects().add(savedProject);
        userRepository.save(user);

        log.info("Created new project with ID: {}", savedProject.getId());
        return projectMapper.toResponse(savedProject);
    }

    @Transactional
    @Override
    public void deleteProject(Long projectId) {
        if(!projectRepository.existsById(projectId)) {
            log.warn("Attempted to delete non-existent project with ID: {}", projectId);
            throw new EntityNotFoundException("Project with ID: " + projectId + " not found");
        }

        projectRepository.deleteById(projectId);
        log.info("Deleted project with ID: {}", projectId);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<ProjectResponse> getProjectsForUser(Long userId,
                                                            ProjectStatus status,
                                                            String title,
                                                            int page,
                                                            int size) {
        Specification<Project> spec = buildProjectSpecification(userId, status, title);
        Page<Project> projectPage = projectRepository.findAll(spec,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        List<ProjectResponse> content = projectPage.getContent().stream()
                .map(projectMapper::toResponse)
                .toList();

        return PageResponse.<ProjectResponse>builder()
                .content(content)
                .pageNumber(projectPage.getNumber())
                .pageSize(projectPage.getSize())
                .totalElements(projectPage.getTotalElements())
                .totalPages(projectPage.getTotalPages())
                .build();
    }

    private Specification<Project> buildProjectSpecification(Long userId, ProjectStatus status, String title) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if(userId != null) {
                Join<Project, User> users = root.join("users");
                predicates.add(cb.equal(users.get("id"), userId));

                if(query != null) {
                    query.distinct(true);
                }
            }

            if(status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if(title != null && !title.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + title.trim().toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
