package com.example.teamsprint.controller;

import com.example.teamsprint.dto.PageResponse;
import com.example.teamsprint.dto.TaskRequest;
import com.example.teamsprint.dto.TaskResponse;
import com.example.teamsprint.entity.enums.TaskPriority;
import com.example.teamsprint.entity.enums.TaskStatus;
import com.example.teamsprint.security.JwtService;
import com.example.teamsprint.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @MockBean
    private JwtService jwtService;

    @Test
    @DisplayName("createTask returns 201 with created task")
    void createTask_returnsCreatedTask() throws Exception {
        TaskRequest request = TaskRequest.builder()
                .title("Task 1")
                .description("Description")
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.TODO)
                .build();

        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 10, 0, 0);
        TaskResponse response = TaskResponse.builder()
                .id(7L)
                .title("Task 1")
                .description("Description")
                .priority(TaskPriority.HIGH)
                .status(TaskStatus.TODO)
                .sprintId(4L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(taskService.createTask(eq(4L), any(TaskRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/sprints/4/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.title").value("Task 1"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.sprintId").value(4));
    }

    @Test
    @DisplayName("getTasksBySprintId returns 200 with task list")
    void getTasksBySprintId_returnsOkWithTaskList() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 1, 2, 11, 0, 0);
        List<TaskResponse> responses = List.of(
                TaskResponse.builder()
                        .id(1L)
                        .title("Task A")
                        .description("First")
                        .priority(TaskPriority.LOW)
                        .status(TaskStatus.TODO)
                        .sprintId(3L)
                        .createdAt(now)
                        .updatedAt(now)
                        .build(),
                TaskResponse.builder()
                        .id(2L)
                        .title("Task B")
                        .description("Second")
                        .priority(TaskPriority.HIGH)
                        .status(TaskStatus.IN_PROGRESS)
                        .sprintId(3L)
                        .createdAt(now)
                        .updatedAt(now)
                        .build()
        );

        PageResponse<TaskResponse> pageResponse = PageResponse.<TaskResponse>builder()
                .content(responses)
                .pageNumber(0)
                .pageSize(20)
                .totalElements(2)
                .totalPages(1)
                .build();

        when(taskService.getTasksBySprintId(eq(3L), any(), any(), anyInt(), anyInt()))
                .thenReturn(pageResponse);

        mockMvc.perform(get("/api/sprints/3/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Task A"))
                .andExpect(jsonPath("$.content[0].priority").value("LOW"))
                .andExpect(jsonPath("$.content[0].status").value("TODO"))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].title").value("Task B"))
                .andExpect(jsonPath("$.content[1].priority").value("HIGH"))
                .andExpect(jsonPath("$.content[1].status").value("IN_PROGRESS"));
    }

    @Test
    @DisplayName("createTask returns 400 when title is blank")
    void createTask_returnsBadRequest_whenTitleIsBlank() throws Exception {
        TaskRequest request = TaskRequest.builder()
                .title(" ")
                .description("Description")
                .priority(TaskPriority.LOW)
                .status(TaskStatus.TODO)
                .build();

        mockMvc.perform(post("/api/sprints/1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("createTask returns 400 when title exceeds max length")
    void createTask_returnsBadRequest_whenTitleExceedsMaxLength() throws Exception {
        TaskRequest request = TaskRequest.builder()
                .title("a".repeat(101))
                .description("Description")
                .priority(TaskPriority.LOW)
                .status(TaskStatus.TODO)
                .build();

        mockMvc.perform(post("/api/sprints/1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("createTask returns 400 when description exceeds max length")
    void createTask_returnsBadRequest_whenDescriptionExceedsMaxLength() throws Exception {
        TaskRequest request = TaskRequest.builder()
                .title("Task")
                .description("a".repeat(501))
                .priority(TaskPriority.LOW)
                .status(TaskStatus.TODO)
                .build();

        mockMvc.perform(post("/api/sprints/1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("createTask returns 400 when priority is null")
    void createTask_returnsBadRequest_whenPriorityIsNull() throws Exception {
        TaskRequest request = TaskRequest.builder()
                .title("Task")
                .description("Description")
                .priority(null)
                .status(TaskStatus.TODO)
                .build();

        mockMvc.perform(post("/api/sprints/1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("createTask returns 400 when status is null")
    void createTask_returnsBadRequest_whenStatusIsNull() throws Exception {
        TaskRequest request = TaskRequest.builder()
                .title("Task")
                .description("Description")
                .priority(TaskPriority.MEDIUM)
                .status(null)
                .build();

        mockMvc.perform(post("/api/sprints/1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/tasks/{taskId}/assign/{userId} returns 200")
    void assignTaskToUser_returnsOk() throws Exception {
        Long taskId = 1L;
        Long userId = 10L;
        TaskResponse response = TaskResponse.builder()
                .id(taskId)
                .title("Updated Task")
                .build();

        when(taskService.assignTaskToUser(taskId, userId)).thenReturn(response);

        mockMvc.perform(patch("/api/tasks/" + taskId + "/assign/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("Updated Task"));
    }
}
