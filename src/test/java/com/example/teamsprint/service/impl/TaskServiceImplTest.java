package com.example.teamsprint.service.impl;

import com.example.teamsprint.dto.TaskRequest;
import com.example.teamsprint.entity.Sprint;
import com.example.teamsprint.entity.Task;
import com.example.teamsprint.entity.enums.SprintStatus;
import com.example.teamsprint.entity.enums.TaskPriority;
import com.example.teamsprint.entity.enums.TaskStatus;
import com.example.teamsprint.exception.EntityNotFoundException;
import com.example.teamsprint.repository.SprintRepository;
import com.example.teamsprint.repository.TaskRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

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

        assertThatThrownBy(() -> taskService.createTask(44L, request))
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

        Sprint sprint = Sprint.builder()
                .id(3L)
                .title("Sprint")
                .status(SprintStatus.ACTIVE)
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
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        var result = taskService.createTask(3L, request);

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

        Sprint sprint = Sprint.builder()
                .id(9L)
                .title("Sprint")
                .status(SprintStatus.PLANNED)
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
        when(taskRepository.save(any(Task.class))).thenReturn(savedTask);

        var result = taskService.createTask(9L, request);

        assertThat(result.getDescription()).isNull();
        assertThat(result.getSprintId()).isEqualTo(9L);
    }

    @Test
    @DisplayName("getTasksBySprintId throws EntityNotFoundException when sprint does not exist")
    void getTasksBySprintId_throwsEntityNotFoundException_whenSprintMissing() {
        when(sprintRepository.existsById(22L)).thenReturn(false);

        assertThatThrownBy(() -> taskService.getTasksBySprintId(22L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Sprint not found with ID: 22");
    }

    @Test
    @DisplayName("getTasksBySprintId returns empty list when no tasks exist")
    void getTasksBySprintId_returnsEmptyList_whenNoTasksExist() {
        when(sprintRepository.existsById(5L)).thenReturn(true);
        when(taskRepository.findBySprintId(5L)).thenReturn(List.of());

        var result = taskService.getTasksBySprintId(5L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getTasksBySprintId maps task entities to responses")
    void getTasksBySprintId_mapsTaskEntitiesToResponses() {
        Sprint sprint = Sprint.builder()
                .id(11L)
                .title("Sprint")
                .status(SprintStatus.ACTIVE)
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

        when(sprintRepository.existsById(11L)).thenReturn(true);
        when(taskRepository.findBySprintId(11L)).thenReturn(List.of(task1, task2));

        var result = taskService.getTasksBySprintId(11L);

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getId()).isEqualTo(1L);
        assertThat(result.getFirst().getTitle()).isEqualTo("Task A");
        assertThat(result.getFirst().getPriority()).isEqualTo(TaskPriority.LOW);
        assertThat(result.getFirst().getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(result.getFirst().getSprintId()).isEqualTo(11L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getTitle()).isEqualTo("Task B");
        assertThat(result.get(1).getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(result.get(1).getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(result.get(1).getSprintId()).isEqualTo(11L);
    }
}
