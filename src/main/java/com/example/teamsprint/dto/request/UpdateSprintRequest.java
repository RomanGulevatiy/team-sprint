package com.example.teamsprint.dto.request;

import com.example.teamsprint.entity.enums.SprintStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateSprintRequest {

    @Size(min = 1, max = 100, message = "Title must be between {min} and {max} characters")
    private String title;

    @Size(max = 500, message = "Description cannot exceed {max} characters")
    private String description;

    private SprintStatus status;
    private LocalDateTime startDate;
    private LocalDateTime dueDate;
}
