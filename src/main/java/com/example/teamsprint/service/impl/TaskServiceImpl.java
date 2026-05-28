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
import com.example.teamsprint.mapper.TaskMapper;
import com.example.teamsprint.repository.ProjectRepository;
import com.example.teamsprint.repository.SprintRepository;
import com.example.teamsprint.repository.TaskRepository;
import com.example.teamsprint.repository.UserRepository;
import com.example.teamsprint.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    private final ProjectRepository projectRepository;
    private final TaskMapper taskMapper;

    @Transactional
    @Override
    public TaskResponse createTask(Long sprintId, TaskRequest taskRequest, Long userId) {

        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new EntityNotFoundException("Sprint not found with ID: " + sprintId));

        Long projectId = sprint.getProject().getId();
        if(!projectRepository.existsByIdAndUsers_Id(projectId, userId)) {
            throw new UserNotInProjectException("User with ID: " + userId + " is not part of project ID: " + projectId);
        }
        Task task = taskMapper.toEntity(taskRequest);
        task.setSprint(sprint);

        Task savedTask = taskRepository.save(task);
        log.info("Created new task with ID: {}", savedTask.getId());
        return taskMapper.toResponse(savedTask);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<TaskResponse> getTasksBySprintId(Long sprintId,
                                                         TaskStatus status,
                                                         TaskPriority priority,
                                                         int page,
                                                         int size,
                                                         Long userId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new EntityNotFoundException("Sprint not found with ID: " + sprintId));

        Long projectId = sprint.getProject().getId();
        if(!projectRepository.existsByIdAndUsers_Id(projectId, userId)) {
            throw new UserNotInProjectException("User with ID: " + userId + " is not part of project ID: " + projectId);
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

        Page<Task> taskPage = taskRepository.findAll(spec,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        List<TaskResponse> content = taskPage.getContent().stream()
                .map(taskMapper::toResponse)
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
    public TaskResponse assignTaskToUser(Long taskId, Long userId, Long requesterId) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with ID: " + taskId));

        Long projectId = task.getSprint().getProject().getId();
        if(!projectRepository.existsByIdAndUsers_Id(projectId, requesterId)) {
            throw new UserNotInProjectException("User with ID: " + requesterId + " is not part of project ID: " + projectId);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        if(!user.getProjects().contains(task.getSprint().getProject())) {
            throw new UserNotInProjectException("User with ID: " + userId + " is not part of the project associated with this task.");
        }

        task.setAssignee(user);
        Task updatedTask = taskRepository.save(task);
        log.info("Assigned task ID: {} to user ID: {}", taskId, userId);
        return taskMapper.toResponse(updatedTask);
    }
}
