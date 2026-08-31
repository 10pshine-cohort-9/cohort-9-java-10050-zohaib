package com.contactmanagement.controller;

import com.contactmanagement.dto.*;
import com.contactmanagement.repository.UserRepository;
import com.contactmanagement.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<PagedResponse<ContactSummaryResponse>> getContacts(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastName") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(required = false) String search) {

        Long userId = resolveUserId(userDetails);
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        log.debug("GET /api/contacts userId={} page={} size={} search='{}'",
                userId, page, size, search);

        PagedResponse<ContactSummaryResponse> response = StringUtils.hasText(search)
                ? contactService.searchContacts(userId, search, pageable)
                : contactService.getContacts(userId, pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContactResponse> getContact(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Long userId = resolveUserId(userDetails);
        log.debug("GET /api/contacts/{} userId={}", id, userId);

        return ResponseEntity.ok(contactService.getContactById(userId, id));
    }

    @PostMapping
    public ResponseEntity<ContactResponse> createContact(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateContactRequest request) {

        Long userId = resolveUserId(userDetails);
        log.debug("POST /api/contacts userId={}", userId);

        ContactResponse response = contactService.createContact(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContactResponse> updateContact(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UpdateContactRequest request) {

        Long userId = resolveUserId(userDetails);
        log.debug("PUT /api/contacts/{} userId={}", id, userId);

        return ResponseEntity.ok(contactService.updateContact(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteContact(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        Long userId = resolveUserId(userDetails);
        log.debug("DELETE /api/contacts/{} userId={}", id, userId);

        contactService.deleteContact(userId, id);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Contact deleted successfully.")
                .build());
    }

    private Long resolveUserId(UserDetails userDetails) {
        String identifier = userDetails.getUsername();
        return userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhone(identifier))
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user not found in database: " + identifier))
                .getId();
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        size = Math.min(size, 100);
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        return PageRequest.of(page, size, sort);
    }
}
