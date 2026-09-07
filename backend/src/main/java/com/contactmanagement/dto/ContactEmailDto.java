package com.contactmanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactEmailDto {

    private Long id;

    @NotBlank(message = "Email label is required")
    @Size(max = 50)
    private String label;

    @NotBlank(message = "Email address is required")
    @Email(message = "Email address is not valid")
    @Size(max = 255)
    private String address;
}
