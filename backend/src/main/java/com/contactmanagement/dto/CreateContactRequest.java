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
    @Size(max = 100)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100)
    private String lastName;

    @Size(max = 100)
    private String title;

    @Size(max = 255)
    private String company;

    @Size(max = 500)
    private String address;

    @Size(max = 2000)
    private String notes;

    @Valid
    private List<ContactPhoneDto> phones = new ArrayList<>();

    @Valid
    private List<ContactEmailDto> emails = new ArrayList<>();
}
