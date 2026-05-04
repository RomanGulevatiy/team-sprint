package com.example.teamsprint.controller;

import com.example.teamsprint.dto.ProjectRequest;
import com.example.teamsprint.dto.ProjectResponse;
import com.example.teamsprint.entity.enums.ProjectStatus;
import com.example.teamsprint.service.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    @Test
    @DisplayName("getAllProjects returns 200 with project list")
    void getAllProjects_returnsOkWithProjectList() throws Exception {
        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 10, 0, 0);
        List<ProjectResponse> responses = List.of(
                ProjectResponse.builder()
                        .id(1L)
                        .title("Project A")
                        .description("First")
                        .status(ProjectStatus.OPEN)
                        .createdAt(now)
                        .updatedAt(now)
                        .build(),
                ProjectResponse.builder()
                        .id(2L)
                        .title("Project B")
                        .description("Second")
                        .status(ProjectStatus.CLOSED)
                        .createdAt(now)
                        .updatedAt(now)
                        .build()
        );

        when(projectService.getAllProjects()).thenReturn(responses);

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Project A"))
                .andExpect(jsonPath("$[0].description").value("First"))
                .andExpect(jsonPath("$[0].status").value("OPEN"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Project B"))
                .andExpect(jsonPath("$[1].description").value("Second"))
                .andExpect(jsonPath("$[1].status").value("CLOSED"));
    }

    @Test
    @DisplayName("createProject returns 201 with created project")
    void createProject_returnsCreatedProject() throws Exception {
        ProjectRequest request = ProjectRequest.builder()
                .title("New Project")
                .description("New Description")
                .status(ProjectStatus.OPEN)
                .build();

        LocalDateTime now = LocalDateTime.of(2026, 1, 1, 10, 0, 0);
        ProjectResponse response = ProjectResponse.builder()
                .id(10L)
                .title("New Project")
                .description("New Description")
                .status(ProjectStatus.OPEN)
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(projectService.createProject(any(ProjectRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("New Project"))
                .andExpect(jsonPath("$.description").value("New Description"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    @DisplayName("createProject returns 400 when title is blank")
    void createProject_returnsBadRequest_whenTitleIsBlank() throws Exception {
        ProjectRequest request = ProjectRequest.builder()
                .title(" ")
                .description("Valid description")
                .status(ProjectStatus.OPEN)
                .build();

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("createProject returns 400 when title exceeds max length")
    void createProject_returnsBadRequest_whenTitleExceedsMaxLength() throws Exception {
        ProjectRequest request = ProjectRequest.builder()
                .title("a".repeat(101))
                .description("Valid description")
                .status(ProjectStatus.OPEN)
                .build();

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("createProject returns 400 when description exceeds max length")
    void createProject_returnsBadRequest_whenDescriptionExceedsMaxLength() throws Exception {
        ProjectRequest request = ProjectRequest.builder()
                .title("Valid title")
                .description("a".repeat(501))
                .status(ProjectStatus.OPEN)
                .build();

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("createProject returns 400 when status is null")
    void createProject_returnsBadRequest_whenStatusIsNull() throws Exception {
        ProjectRequest request = ProjectRequest.builder()
                .title("Valid title")
                .description("Valid description")
                .status(null)
                .build();

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
