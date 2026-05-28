package com.example.teamsprint.dto.request;

import com.example.teamsprint.entity.enums.ProjectStatus;
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
public class ProjectRequest {

    @NotBlank(message = "Title cannot be null")
    @Size(min = 1, max = 100, message = "Title must be between {min} and {max} characters")
    private String title;

    @Size(max = 255, message = "Description cannot exceed {max} characters")
    private String description;

    @NotNull(message = "Status cannot be null")
    private ProjectStatus status;
}
