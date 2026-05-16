package com.example.teamsprint.dto;

import com.example.teamsprint.entity.enums.TaskPriority;
import com.example.teamsprint.entity.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskRequest {

    @NotBlank(message = "Title cannot be null")
    @Size(min = 1, max = 100, message = "Title must be between {min} and {max} characters")
    private String title;

    @Size(max = 500, message = "Description cannot exceed {max} characters")
    private String description;

    @NotNull(message = "Priority cannot be null")
    private TaskPriority priority;

    @NotNull(message = "Status cannot be null")
    private TaskStatus status;
}
