package com.contactmanagement.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactPhoneDto {

    private Long id;

    @NotBlank(message = "Phone label is required")
    @Size(max = 50, message = "Phone label must not exceed 50 characters")
    private String label;

    @NotBlank(message = "Phone number is required")
    @Pattern(
        regexp = "^\\+?[0-9\\s\\-().]{6,30}$",
        message = "Phone number is not valid"
    )
    @Size(max = 50, message = "Phone number must not exceed 50 characters")
    private String number;
}
