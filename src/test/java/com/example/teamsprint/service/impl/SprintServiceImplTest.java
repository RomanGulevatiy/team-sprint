package com.example.teamsprint.service.impl;

import com.example.teamsprint.dto.PageResponse;
import com.example.teamsprint.dto.SprintRequest;
import com.example.teamsprint.dto.SprintResponse;
import com.example.teamsprint.entity.Project;
import com.example.teamsprint.entity.Sprint;
import com.example.teamsprint.entity.enums.ProjectStatus;
import com.example.teamsprint.entity.enums.SprintStatus;
import com.example.teamsprint.exception.EntityNotFoundException;
import com.example.teamsprint.mapper.SprintMapper;
import com.example.teamsprint.repository.ProjectRepository;
import com.example.teamsprint.repository.SprintRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SprintServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private SprintMapper sprintMapper;

    @InjectMocks
    private SprintServiceImpl sprintService;

    private SprintResponse mapResponse(Sprint sprint) {
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

    @Test
    @DisplayName("createSprint throws EntityNotFoundException when project does not exist")
    void createSprint_throwsEntityNotFoundException_whenProjectMissing() {
        SprintRequest request = SprintRequest.builder()
                .title("Sprint")
                .description("Description")
                .status(SprintStatus.PLANNED)
                .build();

        when(projectRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sprintService.createSprint(99L, request, 1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Project not found with ID: 99");
    }

    @Test
    @DisplayName("createSprint returns response with mapped fields")
    void createSprint_returnsResponseWithMappedFields() {
        LocalDateTime startDate = LocalDateTime.of(2026, 1, 1, 9, 0, 0);
        LocalDateTime dueDate = LocalDateTime.of(2026, 1, 15, 18, 0, 0);
        SprintRequest request = SprintRequest.builder()
                .title("Sprint 1")
                .description("Sprint description")
                .status(SprintStatus.ACTIVE)
                .startDate(startDate)
                .dueDate(dueDate)
                .build();

        Project project = Project.builder()
                .id(10L)
                .title("Project")
                .status(ProjectStatus.OPEN)
                .build();

        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 2, 10, 0, 0);
        Sprint savedSprint = Sprint.builder()
                .id(5L)
                .title("Sprint 1")
                .description("Sprint description")
                .status(SprintStatus.ACTIVE)
                .startDate(startDate)
                .dueDate(dueDate)
                .project(project)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        when(projectRepository.findById(10L)).thenReturn(Optional.of(project));
        when(projectRepository.existsByIdAndUsers_Id(10L, 1L)).thenReturn(true);
        when(sprintMapper.toEntity(any(SprintRequest.class))).thenReturn(Sprint.builder().build());
        when(sprintRepository.save(any(Sprint.class))).thenReturn(savedSprint);
        when(sprintMapper.toResponse(savedSprint)).thenReturn(mapResponse(savedSprint));

        var result = sprintService.createSprint(10L, request, 1L);

        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getTitle()).isEqualTo("Sprint 1");
        assertThat(result.getDescription()).isEqualTo("Sprint description");
        assertThat(result.getStatus()).isEqualTo(SprintStatus.ACTIVE);
        assertThat(result.getStartDate()).isEqualTo(startDate);
        assertThat(result.getDueDate()).isEqualTo(dueDate);
        assertThat(result.getProjectId()).isEqualTo(10L);
        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
        assertThat(result.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("createSprint supports null optional fields")
    void createSprint_supportsNullOptionalFields() {
        SprintRequest request = SprintRequest.builder()
                .title("Sprint 2")
                .description(null)
                .status(SprintStatus.PLANNED)
                .startDate(null)
                .dueDate(null)
                .build();

        Project project = Project.builder()
                .id(7L)
                .title("Project")
                .status(ProjectStatus.OPEN)
                .build();

        Sprint savedSprint = Sprint.builder()
                .id(12L)
                .title("Sprint 2")
                .description(null)
                .status(SprintStatus.PLANNED)
                .startDate(null)
                .dueDate(null)
                .project(project)
                .build();

        when(projectRepository.findById(7L)).thenReturn(Optional.of(project));
        when(projectRepository.existsByIdAndUsers_Id(7L, 1L)).thenReturn(true);
        when(sprintMapper.toEntity(any(SprintRequest.class))).thenReturn(Sprint.builder().build());
        when(sprintRepository.save(any(Sprint.class))).thenReturn(savedSprint);
        when(sprintMapper.toResponse(savedSprint)).thenReturn(mapResponse(savedSprint));

        var result = sprintService.createSprint(7L, request, 1L);

        assertThat(result.getDescription()).isNull();
        assertThat(result.getStartDate()).isNull();
        assertThat(result.getDueDate()).isNull();
        assertThat(result.getProjectId()).isEqualTo(7L);
    }

    @Test
    @DisplayName("getSprintsByProjectId throws EntityNotFoundException when project does not exist")
    void getSprintsByProjectId_throwsEntityNotFoundException_whenProjectMissing() {
        when(projectRepository.existsById(31L)).thenReturn(false);

        assertThatThrownBy(() -> sprintService.getSprintsByProjectId(31L, null, 0, 20, 1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Project not found with ID: 31");
    }

    @Test
    @DisplayName("getSprintsByProjectId returns empty list when no sprints exist")
    void getSprintsByProjectId_returnsEmptyList_whenNoSprintsExist() {
        when(projectRepository.existsById(4L)).thenReturn(true);
        when(projectRepository.existsByIdAndUsers_Id(4L, 1L)).thenReturn(true);
        when(sprintRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        PageResponse<?> result = sprintService.getSprintsByProjectId(4L, null, 0, 20, 1L);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("getSprintsByProjectId maps sprint entities to responses")
    void getSprintsByProjectId_mapsSprintEntitiesToResponses() {
        Project project = Project.builder()
                .id(12L)
                .title("Project")
                .status(ProjectStatus.OPEN)
                .build();

        LocalDateTime now = LocalDateTime.of(2026, 1, 8, 9, 0, 0);
        Sprint sprint1 = Sprint.builder()
                .id(1L)
                .title("Sprint A")
                .description("First")
                .status(SprintStatus.ACTIVE)
                .project(project)
                .createdAt(now)
                .updatedAt(now)
                .build();
        Sprint sprint2 = Sprint.builder()
                .id(2L)
                .title("Sprint B")
                .description("Second")
                .status(SprintStatus.PLANNED)
                .project(project)
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(projectRepository.existsById(12L)).thenReturn(true);
        when(projectRepository.existsByIdAndUsers_Id(12L, 1L)).thenReturn(true);
        when(sprintRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sprint1, sprint2), PageRequest.of(0, 20), 2));
        when(sprintMapper.toResponse(sprint1)).thenReturn(mapResponse(sprint1));
        when(sprintMapper.toResponse(sprint2)).thenReturn(mapResponse(sprint2));

        var result = sprintService.getSprintsByProjectId(12L, null, 0, 20, 1L);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().getFirst().getId()).isEqualTo(1L);
        assertThat(result.getContent().getFirst().getTitle()).isEqualTo("Sprint A");
        assertThat(result.getContent().getFirst().getStatus()).isEqualTo(SprintStatus.ACTIVE);
        assertThat(result.getContent().getFirst().getProjectId()).isEqualTo(12L);
        assertThat(result.getContent().get(1).getId()).isEqualTo(2L);
        assertThat(result.getContent().get(1).getTitle()).isEqualTo("Sprint B");
        assertThat(result.getContent().get(1).getStatus()).isEqualTo(SprintStatus.PLANNED);
        assertThat(result.getContent().get(1).getProjectId()).isEqualTo(12L);
    }
}
