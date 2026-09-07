package com.contactmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 4, max = 128, message = "New password must be at least 4 characters")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{4,}$",
        message = "New password must have at least 1 uppercase letter and 1 special character"
    )
    private String newPassword;

    @NotBlank(message = "Password confirmation is required")
    private String confirmNewPassword;
}
