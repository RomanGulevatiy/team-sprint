package com.example.teamsprint.service;

import com.example.teamsprint.dto.request.UpdateTaskRequest;
import com.example.teamsprint.dto.response.PageResponse;
import com.example.teamsprint.dto.request.TaskRequest;
import com.example.teamsprint.dto.response.TaskResponse;
import com.example.teamsprint.entity.enums.TaskPriority;
import com.example.teamsprint.entity.enums.TaskStatus;

public interface TaskService {

    TaskResponse createTask(Long sprintId, TaskRequest taskRequest, Long userId);

    PageResponse<TaskResponse> getTasksBySprintId(Long sprintId,
                                                  TaskStatus status,
                                                  TaskPriority priority,
                                                  int page,
                                                  int size,
                                                  Long userId);

    TaskResponse assignTaskToUser(Long taskId, Long userId, Long requesterId);

    TaskResponse updateTask(Long taskId, UpdateTaskRequest request, Long userId);

    void deleteTask(Long taskId, Long userId);
}
