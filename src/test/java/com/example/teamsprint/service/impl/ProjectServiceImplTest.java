package com.example.teamsprint.service.impl;

import com.example.teamsprint.dto.PageResponse;
import com.example.teamsprint.dto.ProjectRequest;
import com.example.teamsprint.dto.ProjectResponse;
import com.example.teamsprint.entity.Project;
import com.example.teamsprint.entity.User;
import com.example.teamsprint.entity.enums.ProjectStatus;
import com.example.teamsprint.repository.ProjectRepository;
import com.example.teamsprint.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private User buildUser() {
        return User.builder()
                .id(1L)
                .projects(new HashSet<>())
                .build();
    }

    @Test
    @DisplayName("getAllProjects returns empty PageResponse when no projects exist")
    void getAllProjects_returnsEmptyPageResponse_whenNoProjectsExist() {
        Page<Project> emptyPage = new PageImpl<>(Collections.emptyList());
        when(projectRepository.findAll(any(PageRequest.class))).thenReturn(emptyPage);

        PageResponse<ProjectResponse> result = projectService.getAllProjects(0, 10);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getPageNumber()).isZero();
    }

    @Test
    @DisplayName("getAllProjects returns paginated mapped projects")
    void getAllProjects_returnsPaginatedMappedProjects() {
        LocalDateTime now = LocalDateTime.now();
        Project project = Project.builder()
                .id(1L)
                .title("Project 1")
                .status(ProjectStatus.OPEN)
                .createdAt(now)
                .build();

        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Project> projectPage = new PageImpl<>(List.of(project), pageRequest, 1);

        when(projectRepository.findAll(any(PageRequest.of(0, 10).getClass()))).thenReturn(projectPage);

        var result = projectService.getAllProjects(0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getTitle()).isEqualTo("Project 1");
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getPageSize()).isEqualTo(10);
        assertThat(result.getPageNumber()).isEqualTo(0);
    }

    @Test
    @DisplayName("createProject creates and returns project response")
    void createProject_createsAndReturnsProjectResponse() {
        ProjectRequest request = ProjectRequest.builder()
                .title("New Project")
                .description("New Description")
                .status(ProjectStatus.OPEN)
                .build();

        LocalDateTime now = LocalDateTime.now();
        Project savedProject = Project.builder()
                .id(1L)
                .title("New Project")
                .description("New Description")
                .status(ProjectStatus.OPEN)
                .createdAt(now)
                .updatedAt(now)
                .build();

        User user = buildUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

        var result = projectService.createProject(request, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("New Project");
        assertThat(result.getDescription()).isEqualTo("New Description");
        assertThat(result.getStatus()).isEqualTo(ProjectStatus.OPEN);
        assertThat(user.getProjects()).contains(savedProject);
    }

    @Test
    @DisplayName("createProject saves project to repository")
    void createProject_savesProjectToRepository() {
        ProjectRequest request = ProjectRequest.builder()
                .title("New Project")
                .description("Description")
                .status(ProjectStatus.OPEN)
                .build();

        Project savedProject = Project.builder()
                .id(1L)
                .title("New Project")
                .description("Description")
                .status(ProjectStatus.OPEN)
                .build();

        User user = buildUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);
        when(userRepository.save(any(User.class))).thenReturn(user);

        projectService.createProject(request, 1L);

        verify(projectRepository).save(any(Project.class));
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("createProject maps request fields to project entity")
    void createProject_mapsRequestFieldsToProjectEntity() {
        ProjectRequest request = ProjectRequest.builder()
                .title("Mapped Project")
                .description("Mapped Description")
                .status(ProjectStatus.CLOSED)
                .build();

        Project savedProject = Project.builder()
                .id(1L)
                .title("Mapped Project")
                .description("Mapped Description")
                .status(ProjectStatus.CLOSED)
                .build();

        User user = buildUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

        var result = projectService.createProject(request, 1L);

        assertThat(result.getTitle()).isEqualTo("Mapped Project");
        assertThat(result.getDescription()).isEqualTo("Mapped Description");
        assertThat(result.getStatus()).isEqualTo(ProjectStatus.CLOSED);
    }

    @Test
    @DisplayName("createProject succeeds with null description")
    void createProject_succeeds_withNullDescription() {
        ProjectRequest request = ProjectRequest.builder()
                .title("Project Without Description")
                .description(null)
                .status(ProjectStatus.OPEN)
                .build();

        Project savedProject = Project.builder()
                .id(1L)
                .title("Project Without Description")
                .description(null)
                .status(ProjectStatus.OPEN)
                .build();

        User user = buildUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

        var result = projectService.createProject(request, 1L);

        assertThat(result.getDescription()).isNull();
    }

    @Test
    @DisplayName("createProject succeeds with all project statuses")
    void createProject_succeeds_withAllProjectStatuses() {
        User user = buildUser();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        for (ProjectStatus status : ProjectStatus.values()) {
            ProjectRequest request = ProjectRequest.builder()
                    .title("Project")
                    .description("Description")
                    .status(status)
                    .build();

            Project savedProject = Project.builder()
                    .id(1L)
                    .title("Project")
                    .description("Description")
                    .status(status)
                    .build();

            when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

            var result = projectService.createProject(request, 1L);

            assertThat(result.getStatus()).isEqualTo(status);
        }
    }

    @Test
    @DisplayName("createProject returns project response with timestamps")
    void createProject_returnsProjectResponseWithTimestamps() {
        ProjectRequest request = ProjectRequest.builder()
                .title("Project")
                .description("Description")
                .status(ProjectStatus.OPEN)
                .build();

        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 1, 10, 0, 0);

        Project savedProject = Project.builder()
                .id(1L)
                .title("Project")
                .description("Description")
                .status(ProjectStatus.OPEN)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        User user = buildUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

        var result = projectService.createProject(request, 1L);

        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
        assertThat(result.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("createProject maps all fields from saved entity")
    void createProject_mapsAllFieldsFromSavedEntity() {
        ProjectRequest request = ProjectRequest.builder()
                .title("Complete Project")
                .description("Complete Description")
                .status(ProjectStatus.OPEN)
                .build();

        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 2, 15, 30, 0);

        Project savedProject = Project.builder()
                .id(99L)
                .title("Complete Project")
                .description("Complete Description")
                .status(ProjectStatus.OPEN)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        User user = buildUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

        var result = projectService.createProject(request, 1L);

        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getTitle()).isEqualTo("Complete Project");
        assertThat(result.getDescription()).isEqualTo("Complete Description");
        assertThat(result.getStatus()).isEqualTo(ProjectStatus.OPEN);
        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
        assertThat(result.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("getProjectsForUser returns paginated mapped projects")
    void getProjectsForUser_returnsPaginatedMappedProjects() {
        Long userId = 7L;
        LocalDateTime now = LocalDateTime.now();
        Project project = Project.builder()
                .id(2L)
                .title("Assigned Project")
                .status(ProjectStatus.OPEN)
                .createdAt(now)
                .build();

        PageRequest pageRequest = PageRequest.of(0, 5);
        Page<Project> projectPage = new PageImpl<>(List.of(project), pageRequest, 1);

        when(projectRepository.findByUsers_Id(eq(userId), any(PageRequest.class))).thenReturn(projectPage);

        var result = projectService.getProjectsForUser(userId, 0, 5);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getTitle()).isEqualTo("Assigned Project");
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getPageSize()).isEqualTo(5);
        assertThat(result.getPageNumber()).isEqualTo(0);
    }
}
