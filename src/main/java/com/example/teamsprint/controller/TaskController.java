package com.example.teamsprint.controller;

import com.example.teamsprint.dto.TaskRequest;
import com.example.teamsprint.dto.TaskResponse;
import com.example.teamsprint.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping("sprints/{sprintId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(@PathVariable Long sprintId,
                                   @Valid @RequestBody TaskRequest taskRequest) {
        return taskService.createTask(sprintId, taskRequest);
    }

    @GetMapping("sprints/{sprintId}/tasks")
    @ResponseStatus(HttpStatus.OK)
    public List<TaskResponse> getTasksBySprintId(@PathVariable Long sprintId) {
        return taskService.getTasksBySprintId(sprintId);
    }

    @PatchMapping("/tasks/{taskId}/assign/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public TaskResponse assignTask(@PathVariable Long taskId, @PathVariable Long userId) {
        return taskService.assignTaskToUser(taskId, userId);
    }
}
