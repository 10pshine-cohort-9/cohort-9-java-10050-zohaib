package com.contactmanagement.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreateContactRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    private String lastName;

    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    @Size(max = 255, message = "Company must not exceed 255 characters")
    private String company;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Size(max = 2000, message = "Notes must not exceed 2000 characters")
    private String notes;

    @Valid
    private List<ContactPhoneDto> phones = new ArrayList<>();

    @Valid
    private List<ContactEmailDto> emails = new ArrayList<>();
}
