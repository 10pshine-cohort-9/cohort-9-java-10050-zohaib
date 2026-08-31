package com.contactmanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContactSummaryResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String title;
    private String company;
    private String primaryPhone;
    private String primaryEmail;
}
