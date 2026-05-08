package com.example.teamsprint.service;

import com.example.teamsprint.dto.TaskRequest;
import com.example.teamsprint.dto.TaskResponse;

import java.util.List;

public interface TaskService {

    TaskResponse createTask(Long sprintId, TaskRequest taskRequest);

    List<TaskResponse> getTasksBySprintId(Long sprintId);

    TaskResponse assignTaskToUser(Long taskId, Long userId);
}
