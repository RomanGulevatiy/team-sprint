package com.example.teamsprint.service;

import com.example.teamsprint.dto.TaskRequest;
import com.example.teamsprint.dto.TaskResponse;

public interface TaskService {

    TaskResponse createTask(Long sprintId, TaskRequest taskRequest);
}
