package com.example.teamsprint.service.impl;

import com.example.teamsprint.dto.PageResponse;
import com.example.teamsprint.dto.TaskRequest;
import com.example.teamsprint.dto.TaskResponse;
import com.example.teamsprint.entity.Sprint;
import com.example.teamsprint.entity.Task;
import com.example.teamsprint.entity.User;
import com.example.teamsprint.entity.enums.TaskPriority;
import com.example.teamsprint.entity.enums.TaskStatus;
import com.example.teamsprint.exception.EntityNotFoundException;
import com.example.teamsprint.exception.UserNotInProjectException;
import com.example.teamsprint.repository.SprintRepository;
import com.example.teamsprint.repository.TaskRepository;
import com.example.teamsprint.repository.UserRepository;
import com.example.teamsprint.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final SprintRepository sprintRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public TaskResponse createTask(Long sprintId, TaskRequest taskRequest) {

        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new EntityNotFoundException("Sprint not found with ID: " + sprintId));

        Task task = mapToTaskEntity(taskRequest);
        task.setSprint(sprint);

        Task savedTask = taskRepository.save(task);
        log.info("Created new task with ID: {}", savedTask.getId());
        return mapToTaskResponse(savedTask);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<TaskResponse> getTasksBySprintId(Long sprintId,
                                                         TaskStatus status,
                                                         TaskPriority priority,
                                                         int page,
                                                         int size) {
        if(!sprintRepository.existsById(sprintId)) {
            throw new EntityNotFoundException("Sprint not found with ID: " + sprintId);
        }

        Specification<Task> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("sprint").get("id"), sprintId));

            if(status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if(priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Task> taskPage = taskRepository.findAll(spec, PageRequest.of(page, size));
        List<TaskResponse> content = taskPage.getContent().stream()
                .map(this::mapToTaskResponse)
                .toList();

        log.info("Retrieved {} tasks for sprint ID: {}", taskPage.getNumberOfElements(), sprintId);
        return PageResponse.<TaskResponse>builder()
                .content(content)
                .pageNumber(taskPage.getNumber())
                .pageSize(taskPage.getSize())
                .totalElements(taskPage.getTotalElements())
                .totalPages(taskPage.getTotalPages())
                .build();
    }

    @Transactional
    @Override
    public TaskResponse assignTaskToUser(Long taskId, Long userId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with ID: " + taskId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        if (!user.getProjects().contains(task.getSprint().getProject())) {
            throw new UserNotInProjectException("User with ID: " + userId + " is not part of the project associated with this task.");
        }

        task.setAssignee(user);
        Task updatedTask = taskRepository.save(task);
        log.info("Assigned task ID: {} to user ID: {}", taskId, userId);
        return mapToTaskResponse(updatedTask);
    }

    /**
     * Helper method to map TaskRequest DTO to Task entity
     *
     * @param taskRequest the TaskRequest DTO to be mapped
     * @return the corresponding Task entity
     */
    private Task mapToTaskEntity(TaskRequest taskRequest) {
        return Task.builder()
                .title(taskRequest.getTitle())
                .description(taskRequest.getDescription())
                .priority(taskRequest.getPriority())
                .status(taskRequest.getStatus())
                .build();
    }

    /**
     * Helper method to map Task entity to TaskResponse DTO
     *
     * @param task the Task entity to be mapped
     * @return the corresponding TaskResponse DTO
     */
    private TaskResponse mapToTaskResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .priority(task.getPriority())
                .status(task.getStatus())
                .sprintId(task.getSprint().getId())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
