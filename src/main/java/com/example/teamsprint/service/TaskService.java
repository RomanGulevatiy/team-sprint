package com.example.teamsprint.service;

import com.example.teamsprint.dto.PageResponse;
import com.example.teamsprint.dto.TaskRequest;
import com.example.teamsprint.dto.TaskResponse;
import com.example.teamsprint.entity.enums.TaskPriority;
import com.example.teamsprint.entity.enums.TaskStatus;

public interface TaskService {

    TaskResponse createTask(Long sprintId, TaskRequest taskRequest);

    PageResponse<TaskResponse> getTasksBySprintId(Long sprintId,
                                                  TaskStatus status,
                                                  TaskPriority priority,
                                                  int page,
                                                  int size);

    TaskResponse assignTaskToUser(Long taskId, Long userId);
}
