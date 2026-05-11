package com.example.teamsprint.controller;

import com.example.teamsprint.dto.PageResponse;
import com.example.teamsprint.dto.ProjectRequest;
import com.example.teamsprint.dto.ProjectResponse;
import com.example.teamsprint.entity.enums.ProjectStatus;
import com.example.teamsprint.security.JwtService;
import com.example.teamsprint.service.ProjectService;
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

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private JwtService jwtService;

    @Test
    @DisplayName("getAllProjects returns 200 with paginated project response")
    void getAllProjects_returnsOkWithPaginatedResponse() throws Exception {
        List<ProjectResponse> content = List.of(
                ProjectResponse.builder()
                        .id(1L)
                        .title("Project A")
                        .status(ProjectStatus.OPEN)
                        .build()
        );

        PageResponse<ProjectResponse> pageResponse = PageResponse.<ProjectResponse>builder()
                .content(content)
                .pageNumber(0)
                .pageSize(10)
                .totalElements(1L)
                .totalPages(1)
                .build();

        when(projectService.getAllProjects(0, 10)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/projects")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(0))
                .andExpect(jsonPath("$.pageSize").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Project A"));
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
