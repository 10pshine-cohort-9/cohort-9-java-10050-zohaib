package com.contactmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String title;
    private String company;
    private String address;
    private String notes;
    private List<ContactPhoneDto> phones;
    private List<ContactEmailDto> emails;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
