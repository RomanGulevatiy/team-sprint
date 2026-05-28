package com.example.teamsprint.dto.response;

import com.example.teamsprint.entity.enums.SprintStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SprintResponse {

    private Long id;
    private String title;
    private String description;
    private SprintStatus status;
    private LocalDateTime startDate;
    private LocalDateTime dueDate;
    private Long projectId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
