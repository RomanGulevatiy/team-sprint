package com.example.teamsprint.controller;

import com.example.teamsprint.dto.TaskRequest;
import com.example.teamsprint.dto.TaskResponse;
import com.example.teamsprint.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @Operation(summary = "Get all tasks for a sprint", description = "Returns a list of all tasks associated with the specified sprint")
    @GetMapping("sprints/{sprintId}/tasks")
    @ResponseStatus(HttpStatus.OK)
    public List<TaskResponse> getTasksBySprintId(@PathVariable Long sprintId) {
        return taskService.getTasksBySprintId(sprintId);
    }

    @Operation(summary = "Assign a task to a user", description = "Assigns the specified task to the specified user")
    @PatchMapping("/tasks/{taskId}/assign/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public TaskResponse assignTask(@PathVariable Long taskId, @PathVariable Long userId) {
        return taskService.assignTaskToUser(taskId, userId);
    }
}
