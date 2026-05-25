package com.example.teamsprint.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterRequest {

    @Size(min = 3, max = 20, message = "Username must be {min}-{max} characters long")
    @Pattern(
            regexp = "^[A-Za-z0-9_]+$",
            message = "Username must contain only English letters and digits (no spaces or hyphens)"
    )
    @NotBlank(message = "Username cannot be null")
    private String username;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email cannot be null")
    private String email;

    @NotBlank(message = "Password cannot be null")
    private String password;
}
