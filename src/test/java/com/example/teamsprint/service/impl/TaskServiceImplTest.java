package com.example.teamsprint.service.impl;

import com.example.teamsprint.dto.response.PageResponse;
import com.example.teamsprint.dto.request.TaskRequest;
import com.example.teamsprint.dto.response.TaskResponse;
import com.example.teamsprint.entity.Project;
import com.example.teamsprint.entity.Sprint;
import com.example.teamsprint.entity.Task;
import com.example.teamsprint.entity.User;
import com.example.teamsprint.entity.enums.SprintStatus;
import com.example.teamsprint.entity.enums.TaskPriority;
import com.example.teamsprint.entity.enums.TaskStatus;
import com.example.teamsprint.exception.EntityNotFoundException;
import com.example.teamsprint.exception.UserNotInProjectException;
import com.example.teamsprint.mapper.TaskMapper;
import com.example.teamsprint.repository.SprintRepository;
import com.example.teamsprint.repository.TaskRepository;
import com.example.teamsprint.repository.UserRepository;
import com.example.teamsprint.repository.ProjectRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    private TaskResponse mapResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .priority(task.getPriority())
                .status(task.getStatus())
                .sprintId(task.getSprint().getId())
                .assigneeId(task.getAssignee() != null ? task.getAssignee().getId() : null)
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }

    @Test
    @DisplayName("createTask throws EntityNotFoundException when sprint does not exist")
    void createTask_throwsEntityNotFoundException_whenSprintMissing() {
        TaskRequest request = TaskRequest.builder()
                .title("Task")
                .description("Description")
                .priority(TaskPriority.MEDIUM)
                .status(TaskStatus.TODO)
                .build();

        when(sprintRepository.findById(44L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask(44L, request, 1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Sprint not found with ID: 44");
    }

    @Test
    @DisplayName("createTask returns response with mapped fields")
    void createTask_returnsResponseWithMappedFields() {
        TaskRequest request = TaskRequest.builder()
                .title("Task 1")
                .description("Task description")
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.IN_PROGRESS)
                .build();

        Project project = Project.builder().id(30L).build();
        Sprint sprint = Sprint.builder()
                .id(3L)
                .title("Sprint")
                .status(SprintStatus.ACTIVE)
                .project(project)
                .build();

        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 5, 9, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 6, 12, 0, 0);
        Task savedTask = Task.builder()
                .id(8L)
                .title("Task 1")
                .description("Task description")
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.IN_PROGRESS)
                .sprint(sprint)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        when(sprintRepository.findById(3L)).thenReturn(Optional.of(sprint));
        when(projectRepository.existsByIdAndUsers_Id(30L, 1L)).thenReturn(true);
        when(taskMapper.toEntity(any(TaskRequest.class))).thenReturn(Task.builder().build());
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
        when(taskMapper.toResponse(savedTask)).thenReturn(mapResponse(savedTask));

        var result = taskService.createTask(3L, request, 1L);

        assertThat(result.getId()).isEqualTo(8L);
        assertThat(result.getTitle()).isEqualTo("Task 1");
        assertThat(result.getDescription()).isEqualTo("Task description");
        assertThat(result.getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(result.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(result.getSprintId()).isEqualTo(3L);
        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
        assertThat(result.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("createTask supports null description")
    void createTask_supportsNullDescription() {
        TaskRequest request = TaskRequest.builder()
                .title("Task 2")
                .description(null)
                .priority(TaskPriority.LOW)
                .status(TaskStatus.TODO)
                .build();

        Project project = Project.builder().id(90L).build();
        Sprint sprint = Sprint.builder()
                .id(9L)
                .title("Sprint")
                .status(SprintStatus.PLANNED)
                .project(project)
                .build();

        Task savedTask = Task.builder()
                .id(15L)
                .title("Task 2")
                .description(null)
                .priority(TaskPriority.LOW)
                .status(TaskStatus.TODO)
                .sprint(sprint)
                .build();

        when(sprintRepository.findById(9L)).thenReturn(Optional.of(sprint));
        when(projectRepository.existsByIdAndUsers_Id(90L, 1L)).thenReturn(true);
        when(taskMapper.toEntity(any(TaskRequest.class))).thenReturn(Task.builder().build());
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);
        when(taskMapper.toResponse(savedTask)).thenReturn(mapResponse(savedTask));

        var result = taskService.createTask(9L, request, 1L);

        assertThat(result.getDescription()).isNull();
        assertThat(result.getSprintId()).isEqualTo(9L);
    }

    @Test
    @DisplayName("getTasksBySprintId throws EntityNotFoundException when sprint does not exist")
    void getTasksBySprintId_throwsEntityNotFoundException_whenSprintMissing() {
        when(sprintRepository.findById(22L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTasksBySprintId(22L, null, null, 0, 20, 1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Sprint not found with ID: 22");
    }

    @Test
    @DisplayName("getTasksBySprintId returns empty list when no tasks exist")
    void getTasksBySprintId_returnsEmptyList_whenNoTasksExist() {
        Sprint sprint = Sprint.builder().id(5L).project(Project.builder().id(50L).build()).build();
        when(sprintRepository.findById(5L)).thenReturn(Optional.of(sprint));
        when(projectRepository.existsByIdAndUsers_Id(50L, 1L)).thenReturn(true);
        when(taskRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        PageResponse<?> result = taskService.getTasksBySprintId(5L, null, null, 0, 20, 1L);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("getTasksBySprintId maps task entities to responses")
    void getTasksBySprintId_mapsTaskEntitiesToResponses() {
        Project project = Project.builder().id(110L).build();
        Sprint sprint = Sprint.builder()
                .id(11L)
                .title("Sprint")
                .status(SprintStatus.ACTIVE)
                .project(project)
                .build();

        LocalDateTime now = LocalDateTime.of(2026, 1, 7, 10, 0, 0);
        Task task1 = Task.builder()
                .id(1L)
                .title("Task A")
                .description("First")
                .priority(TaskPriority.LOW)
                .status(TaskStatus.TODO)
                .sprint(sprint)
                .createdAt(now)
                .updatedAt(now)
                .build();
        Task task2 = Task.builder()
                .id(2L)
                .title("Task B")
                .description("Second")
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.IN_PROGRESS)
                .sprint(sprint)
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(sprintRepository.findById(11L)).thenReturn(Optional.of(sprint));
        when(projectRepository.existsByIdAndUsers_Id(110L, 1L)).thenReturn(true);
        when(taskRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(task1, task2), PageRequest.of(0, 20), 2));
        when(taskMapper.toResponse(task1)).thenReturn(mapResponse(task1));
        when(taskMapper.toResponse(task2)).thenReturn(mapResponse(task2));

        var result = taskService.getTasksBySprintId(11L, null, null, 0, 20, 1L);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().getFirst().getId()).isEqualTo(1L);
        assertThat(result.getContent().getFirst().getTitle()).isEqualTo("Task A");
        assertThat(result.getContent().getFirst().getPriority()).isEqualTo(TaskPriority.LOW);
        assertThat(result.getContent().getFirst().getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(result.getContent().getFirst().getSprintId()).isEqualTo(11L);
        assertThat(result.getContent().get(1).getId()).isEqualTo(2L);
        assertThat(result.getContent().get(1).getTitle()).isEqualTo("Task B");
        assertThat(result.getContent().get(1).getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(result.getContent().get(1).getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(result.getContent().get(1).getSprintId()).isEqualTo(11L);
    }

    @Test
    @DisplayName("assignTaskToUser successfully assigns user when in same project")
    void assignTaskToUser_success() {
        Long taskId = 1L;
        Long userId = 10L;
        Long projectId = 5L;

        Project project = Project.builder().id(projectId).build();
        Sprint sprint = Sprint.builder().id(2L).project(project).build();
        Task task = Task.builder().id(taskId).sprint(sprint).build();

        User user = User.builder()
                .id(userId)
                .projects(new java.util.HashSet<>(List.of(project)))
                .build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(projectRepository.existsByIdAndUsers_Id(projectId, 1L)).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskMapper.toResponse(task)).thenReturn(mapResponse(task));

        taskService.assignTaskToUser(taskId, userId, 1L);

        assertThat(task.getAssignee()).isEqualTo(user);
        verify(taskRepository).save(task);
    }

    @Test
    @DisplayName("assignTaskToUser throws IllegalArgumentException when user not in project")
    void assignTaskToUser_throwsException_whenUserNotInProject() {
        Project projectA = Project.builder().id(1L).build();
        Project projectB = Project.builder().id(2L).build();

        Sprint sprint = Sprint.builder().project(projectA).build();
        Task task = Task.builder().id(1L).sprint(sprint).build();

        User user = User.builder().id(10L).projects(new java.util.HashSet<>(List.of(projectB))).build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(projectRepository.existsByIdAndUsers_Id(1L, 1L)).thenReturn(true);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> taskService.assignTaskToUser(1L, 10L, 1L))
                .isInstanceOf(UserNotInProjectException.class)
                .hasMessageContaining("is not part of the project associated with this task");
    }

    @Test
    @DisplayName("assignTaskToUser throws EntityNotFoundException when task missing")
    void assignTaskToUser_throwsException_whenTaskNotFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.assignTaskToUser(1L, 10L, 1L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
