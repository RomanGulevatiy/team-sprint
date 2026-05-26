package com.example.teamsprint.controller;

import com.example.teamsprint.dto.PageResponse;
import com.example.teamsprint.dto.TaskRequest;
import com.example.teamsprint.dto.TaskResponse;
import com.example.teamsprint.entity.enums.TaskPriority;
import com.example.teamsprint.entity.enums.TaskStatus;
import com.example.teamsprint.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
                                   @Valid @RequestBody TaskRequest taskRequest) {
        return taskService.createTask(sprintId, taskRequest);
    }

    @Operation(summary = "Get all tasks for a sprint", description = "Returns a paginated list of tasks with optional filters")
    @GetMapping("sprints/{sprintId}/tasks")
    @ResponseStatus(HttpStatus.OK)
    public PageResponse<TaskResponse> getTasksBySprintId(@PathVariable Long sprintId,
                                                         @RequestParam(required = false) TaskStatus status,
                                                         @RequestParam(required = false) TaskPriority priority,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        return taskService.getTasksBySprintId(sprintId,
                status,
                priority,
                page,
                size);
    }

    @Operation(summary = "Assign a task to a user", description = "Assigns the specified task to the specified user")
    @PatchMapping("/tasks/{taskId}/assign/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public TaskResponse assignTask(@PathVariable Long taskId, @PathVariable Long userId) {
        return taskService.assignTaskToUser(taskId, userId);
    }
}
