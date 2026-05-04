package com.example.teamsprint.controller;

import com.example.teamsprint.dto.TaskRequest;
import com.example.teamsprint.dto.TaskResponse;
import com.example.teamsprint.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping("sprints/{sprintId}/tasks")
    public TaskResponse createTask(@PathVariable Long sprintId,
                                   @Valid @RequestBody TaskRequest taskRequest) {
        return taskService.createTask(sprintId, taskRequest);
    }
}
