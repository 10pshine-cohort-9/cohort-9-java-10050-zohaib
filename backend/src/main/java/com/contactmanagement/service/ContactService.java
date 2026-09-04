package com.contactmanagement.service;

import com.contactmanagement.dto.*;
import org.springframework.data.domain.Pageable;

public interface ContactService {

    PagedResponse<ContactSummaryResponse> getContacts(Long userId, Pageable pageable);

    PagedResponse<ContactSummaryResponse> searchContacts(Long userId, String query, Pageable pageable);

    ContactResponse getContactById(Long userId, Long contactId);

    ContactResponse createContact(Long userId, CreateContactRequest request);

    ContactResponse updateContact(Long userId, Long contactId, UpdateContactRequest request);

    void deleteContact(Long userId, Long contactId);
}
