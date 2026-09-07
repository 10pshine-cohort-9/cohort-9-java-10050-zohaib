package com.contactmanagement.service.impl;

import com.contactmanagement.dto.*;
import com.contactmanagement.exception.ResourceNotFoundException;
import com.contactmanagement.model.Contact;
import com.contactmanagement.model.ContactEmail;
import com.contactmanagement.model.ContactPhone;
import com.contactmanagement.repository.ContactRepository;
import com.contactmanagement.service.ContactService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContactServiceImpl implements ContactService {

    private final ContactRepository contactRepository;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ContactSummaryResponse> getContacts(Long userId, Pageable pageable) {
        log.debug("Fetching contacts for userId={} page={} size={}",
                userId, pageable.getPageNumber(), pageable.getPageSize());
        Page<Contact> page = contactRepository.findByUserId(userId, pageable);
        return toPagedSummary(page);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ContactSummaryResponse> searchContacts(Long userId, String query, Pageable pageable) {
        log.debug("Searching contacts for userId={} query='{}' page={} size={}",
                userId, query, pageable.getPageNumber(), pageable.getPageSize());
        Page<Contact> page = contactRepository.searchByUserIdAndQuery(userId, query.trim(), pageable);
        return toPagedSummary(page);
    }

    @Override
    @Transactional(readOnly = true)
    public ContactResponse getContactById(Long userId, Long contactId) {
        log.debug("Fetching contactId={} for userId={}", contactId, userId);
        Contact contact = findOwnedContact(userId, contactId);
        return toContactResponse(contact);
    }

    @Override
    @Transactional
    public ContactResponse createContact(Long userId, CreateContactRequest request) {
        log.debug("Creating contact for userId={} name='{} {}'",
                userId, request.getFirstName(), request.getLastName());

        Contact contact = Contact.builder()
                .userId(userId)
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .title(trimOrNull(request.getTitle()))
                .company(trimOrNull(request.getCompany()))
                .address(trimOrNull(request.getAddress()))
                .notes(trimOrNull(request.getNotes()))
                .build();

        applyPhones(contact, request.getPhones());
        applyEmails(contact, request.getEmails());

        contact = contactRepository.save(contact);
        log.info("Created contact id={} for userId={}", contact.getId(), userId);
        return toContactResponse(contact);
    }

    @Override
    @Transactional
    public ContactResponse updateContact(Long userId, Long contactId, UpdateContactRequest request) {
        log.debug("Updating contactId={} for userId={}", contactId, userId);

        Contact contact = findOwnedContact(userId, contactId);

        contact.setFirstName(request.getFirstName().trim());
        contact.setLastName(request.getLastName().trim());
        contact.setTitle(trimOrNull(request.getTitle()));
        contact.setCompany(trimOrNull(request.getCompany()));
        contact.setAddress(trimOrNull(request.getAddress()));
        contact.setNotes(trimOrNull(request.getNotes()));

        contact.getPhones().clear();
        contact.getEmails().clear();

        applyPhones(contact, request.getPhones());
        applyEmails(contact, request.getEmails());

        contact = contactRepository.save(contact);
        log.info("Updated contact id={} for userId={}", contact.getId(), userId);
        return toContactResponse(contact);
    }

    @Override
    @Transactional
    public void deleteContact(Long userId, Long contactId) {
        log.debug("Deleting contactId={} for userId={}", contactId, userId);
        Contact contact = findOwnedContact(userId, contactId);
        contact.setDeleted(true);
        contactRepository.save(contact);
        log.info("Soft-deleted contact id={} for userId={}", contactId, userId);
    }

    private Contact findOwnedContact(Long userId, Long contactId) {
        return contactRepository.findById(contactId)
                .filter(c -> c.getUserId().equals(userId))
                .orElseThrow(() -> {
                    log.warn("Contact id={} not found or not owned by userId={}", contactId, userId);
                    return new ResourceNotFoundException("Contact", "id", contactId);
                });
    }

    private void applyPhones(Contact contact, List<ContactPhoneDto> dtos) {
        if (dtos == null) return;
        dtos.forEach(dto -> contact.addPhone(
                ContactPhone.builder()
                        .label(dto.getLabel().trim())
                        .number(dto.getNumber().trim())
                        .build()));
    }

    private void applyEmails(Contact contact, List<ContactEmailDto> dtos) {
        if (dtos == null) return;
        dtos.forEach(dto -> contact.addEmail(
                ContactEmail.builder()
                        .label(dto.getLabel().trim())
                        .address(dto.getAddress().trim().toLowerCase())
                        .build()));
    }

    private String trimOrNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private PagedResponse<ContactSummaryResponse> toPagedSummary(Page<Contact> page) {
        List<ContactSummaryResponse> content = page.getContent()
                .stream()
                .map(this::toSummary)
                .toList();

        return PagedResponse.<ContactSummaryResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    private ContactSummaryResponse toSummary(Contact c) {
        return ContactSummaryResponse.builder()
                .id(c.getId())
                .firstName(c.getFirstName())
                .lastName(c.getLastName())
                .title(c.getTitle())
                .company(c.getCompany())
                .primaryPhone(c.getPhones().isEmpty() ? null : c.getPhones().get(0).getNumber())
                .primaryEmail(c.getEmails().isEmpty() ? null : c.getEmails().get(0).getAddress())
                .build();
    }

    private ContactResponse toContactResponse(Contact c) {
        List<ContactPhoneDto> phones = c.getPhones().stream()
                .map(p -> ContactPhoneDto.builder()
                        .id(p.getId()).label(p.getLabel()).number(p.getNumber()).build())
                .toList();

        List<ContactEmailDto> emails = c.getEmails().stream()
                .map(e -> ContactEmailDto.builder()
                        .id(e.getId()).label(e.getLabel()).address(e.getAddress()).build())
                .toList();

        return ContactResponse.builder()
                .id(c.getId())
                .firstName(c.getFirstName())
                .lastName(c.getLastName())
                .title(c.getTitle())
                .company(c.getCompany())
                .address(c.getAddress())
                .notes(c.getNotes())
                .phones(phones)
                .emails(emails)
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
