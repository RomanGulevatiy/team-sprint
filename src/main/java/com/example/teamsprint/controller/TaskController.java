package com.example.teamsprint.controller;

import com.example.teamsprint.dto.request.UpdateTaskRequest;
import com.example.teamsprint.dto.response.PageResponse;
import com.example.teamsprint.dto.request.TaskRequest;
import com.example.teamsprint.dto.response.TaskResponse;
import com.example.teamsprint.entity.enums.TaskPriority;
import com.example.teamsprint.entity.enums.TaskStatus;
import com.example.teamsprint.security.UserPrincipal;
import com.example.teamsprint.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Task Management", description = "Endpoints for managing tasks within sprints")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Create a new task for a sprint", description = "Creates a new task under the specified sprint")
    @PostMapping("sprints/{sprintId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(@PathVariable Long sprintId,
                                   @AuthenticationPrincipal UserPrincipal principal,
                                   @Valid @RequestBody TaskRequest taskRequest) {
        return taskService.createTask(sprintId, taskRequest, principal.getId());
    }

    @Operation(summary = "Get all tasks for a sprint", description = "Returns a paginated list of tasks with optional filters")
    @GetMapping("sprints/{sprintId}/tasks")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<TaskResponse> getTasksBySprintId(@PathVariable Long sprintId,
                                                         @AuthenticationPrincipal UserPrincipal principal,
                                                         @RequestParam(required = false) TaskStatus status,
                                                         @RequestParam(required = false) TaskPriority priority,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "20") int size) {
        return taskService.getTasksBySprintId(sprintId,
                status,
                priority,
                page,
                size,
                principal.getId());
    }

    @Operation(summary = "Assign a task to a user", description = "Assigns the specified task to the specified user")
    @PatchMapping("/tasks/{taskId}/assign/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public TaskResponse assignTask(@PathVariable Long taskId,
                                   @PathVariable Long userId,
                                   @AuthenticationPrincipal UserPrincipal principal) {
        return taskService.assignTaskToUser(taskId, userId, principal.getId());
    }

    @Operation(summary = "Update a task", description = "Updates the details of an existing task")
    @PatchMapping("/tasks/{taskId}")
    @ResponseStatus(HttpStatus.OK)
    public TaskResponse updateTask(@PathVariable Long taskId,
                                   @AuthenticationPrincipal UserPrincipal principal,
                                   @Valid @RequestBody UpdateTaskRequest request) {
        return taskService.updateTask(taskId, request, principal.getId());
    }

    @Operation(summary = "Delete a task", description = "Deletes an existing task by its ID")
    @DeleteMapping("/tasks/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long taskId,
                           @AuthenticationPrincipal UserPrincipal principal) {
        taskService.deleteTask(taskId, principal.getId());
    }
}
