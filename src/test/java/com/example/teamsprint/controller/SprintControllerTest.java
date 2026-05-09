package com.example.teamsprint.controller;

import com.example.teamsprint.dto.SprintRequest;
import com.example.teamsprint.dto.SprintResponse;
import com.example.teamsprint.entity.enums.SprintStatus;
import com.example.teamsprint.service.SprintService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.hasSize;

@WebMvcTest(SprintController.class)
@AutoConfigureMockMvc(addFilters = false)
class SprintControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SprintService sprintService;

    @Test
    @DisplayName("createSprint returns 201 with created sprint")
    void createSprint_returnsCreatedSprint() throws Exception {
        SprintRequest request = SprintRequest.builder()
                .title("Sprint 1")
                .description("Description")
                .status(SprintStatus.PLANNED)
                .startDate(LocalDateTime.of(2026, 1, 1, 9, 0, 0))
                .dueDate(LocalDateTime.of(2026, 1, 10, 18, 0, 0))
                .build();

        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 10, 0, 0);
        SprintResponse response = SprintResponse.builder()
                .id(5L)
                .title("Sprint 1")
                .description("Description")
                .status(SprintStatus.PLANNED)
                .startDate(request.getStartDate())
                .dueDate(request.getDueDate())
                .projectId(2L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(sprintService.createSprint(eq(2L), any(SprintRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/projects/2/sprints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.title").value("Sprint 1"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.status").value("PLANNED"))
                .andExpect(jsonPath("$.projectId").value(2));
    }

    @Test
    @DisplayName("createSprint returns 400 when title is blank")
    void createSprint_returnsBadRequest_whenTitleIsBlank() throws Exception {
        SprintRequest request = SprintRequest.builder()
                .title(" ")
                .description("Description")
                .status(SprintStatus.PLANNED)
                .build();

        mockMvc.perform(post("/api/projects/1/sprints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("createSprint returns 400 when title exceeds max length")
    void createSprint_returnsBadRequest_whenTitleExceedsMaxLength() throws Exception {
        SprintRequest request = SprintRequest.builder()
                .title("a".repeat(101))
                .description("Description")
                .status(SprintStatus.PLANNED)
                .build();

        mockMvc.perform(post("/api/projects/1/sprints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("createSprint returns 400 when description exceeds max length")
    void createSprint_returnsBadRequest_whenDescriptionExceedsMaxLength() throws Exception {
        SprintRequest request = SprintRequest.builder()
                .title("Sprint")
                .description("a".repeat(501))
                .status(SprintStatus.PLANNED)
                .build();

        mockMvc.perform(post("/api/projects/1/sprints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("createSprint returns 400 when status is null")
    void createSprint_returnsBadRequest_whenStatusIsNull() throws Exception {
        SprintRequest request = SprintRequest.builder()
                .title("Sprint")
                .description("Description")
                .status(null)
                .build();

        mockMvc.perform(post("/api/projects/1/sprints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("getSprintsByProjectId returns 200 with sprint list")
    void getSprintsByProjectId_returnsOkWithSprintList() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 1, 3, 9, 0, 0);
        List<SprintResponse> responses = List.of(
                SprintResponse.builder()
                        .id(1L)
                        .title("Sprint A")
                        .description("First")
                        .status(SprintStatus.ACTIVE)
                        .projectId(6L)
                        .createdAt(now)
                        .updatedAt(now)
                        .build(),
                SprintResponse.builder()
                        .id(2L)
                        .title("Sprint B")
                        .description("Second")
                        .status(SprintStatus.PLANNED)
                        .projectId(6L)
                        .createdAt(now)
                        .updatedAt(now)
                        .build()
        );

        when(sprintService.getSprintsByProjectId(6L)).thenReturn(responses);

        mockMvc.perform(get("/api/projects/6/sprints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Sprint A"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Sprint B"))
                .andExpect(jsonPath("$[1].status").value("PLANNED"));
    }
}
