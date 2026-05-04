package com.example.teamsprint.service.impl;

import com.example.teamsprint.dto.ProjectRequest;
import com.example.teamsprint.dto.ProjectResponse;
import com.example.teamsprint.entity.Project;
import com.example.teamsprint.entity.enums.ProjectStatus;
import com.example.teamsprint.repository.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectServiceImpl projectService;

    @Test
    @DisplayName("getAllProjects returns empty list when no projects exist")
    void getAllProjects_returnsEmptyList_whenNoProjectsExist() {
        when(projectRepository.findAll()).thenReturn(new ArrayList<>());

        var result = projectService.getAllProjects();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getAllProjects maps projects to responses when projects exist")
    void getAllProjects_returnsMappedProjectResponses_whenProjectsExist() {
        LocalDateTime now = LocalDateTime.now();
        Project project1 = Project.builder()
                .id(1L)
                .title("Project 1")
                .description("Description 1")
                .status(ProjectStatus.OPEN)
                .createdAt(now)
                .updatedAt(now)
                .build();

        Project project2 = Project.builder()
                .id(2L)
                .title("Project 2")
                .description("Description 2")
                .status(ProjectStatus.CLOSED)
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(projectRepository.findAll()).thenReturn(List.of(project1, project2));

        var result = projectService.getAllProjects();

        assertThat(result)
                .hasSize(2)
                .extracting(ProjectResponse::getId)
                .containsExactly(1L, 2L);
        assertThat(result)
                .extracting(ProjectResponse::getTitle)
                .containsExactly("Project 1", "Project 2");
        assertThat(result)
                .extracting(ProjectResponse::getStatus)
                .containsExactly(ProjectStatus.OPEN, ProjectStatus.CLOSED);
    }

    @Test
    @DisplayName("getAllProjects maps all fields correctly from project entity")
    void getAllProjects_mapsAllFieldsCorrectly_fromProjectEntity() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 2, 15, 30, 0);

        Project project = Project.builder()
                .id(5L)
                .title("Complete Project")
                .description("Full description with details")
                .status(ProjectStatus.OPEN)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        when(projectRepository.findAll()).thenReturn(List.of(project));

        var result = projectService.getAllProjects();

        assertThat(result).hasSize(1);
        ProjectResponse response = result.getFirst();
        assertThat(response.getId()).isEqualTo(5L);
        assertThat(response.getTitle()).isEqualTo("Complete Project");
        assertThat(response.getDescription()).isEqualTo("Full description with details");
        assertThat(response.getStatus()).isEqualTo(ProjectStatus.OPEN);
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
        assertThat(response.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("getAllProjects handles project with null description")
    void getAllProjects_handlesProjectWithNullDescription() {
        LocalDateTime now = LocalDateTime.now();
        Project project = Project.builder()
                .id(1L)
                .title("Project Without Description")
                .description(null)
                .status(ProjectStatus.OPEN)
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(projectRepository.findAll()).thenReturn(List.of(project));

        var result = projectService.getAllProjects();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getDescription()).isNull();
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

        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

        var result = projectService.createProject(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("New Project");
        assertThat(result.getDescription()).isEqualTo("New Description");
        assertThat(result.getStatus()).isEqualTo(ProjectStatus.OPEN);
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

        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

        projectService.createProject(request);

        verify(projectRepository).save(any(Project.class));
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

        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

        var result = projectService.createProject(request);

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

        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

        var result = projectService.createProject(request);

        assertThat(result.getDescription()).isNull();
    }

    @Test
    @DisplayName("createProject succeeds with all project statuses")
    void createProject_succeeds_withAllProjectStatuses() {
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

            var result = projectService.createProject(request);

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

        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

        var result = projectService.createProject(request);

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

        when(projectRepository.save(any(Project.class))).thenReturn(savedProject);

        var result = projectService.createProject(request);

        assertThat(result.getId()).isEqualTo(99L);
        assertThat(result.getTitle()).isEqualTo("Complete Project");
        assertThat(result.getDescription()).isEqualTo("Complete Description");
        assertThat(result.getStatus()).isEqualTo(ProjectStatus.OPEN);
        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
        assertThat(result.getUpdatedAt()).isEqualTo(updatedAt);
    }
}
