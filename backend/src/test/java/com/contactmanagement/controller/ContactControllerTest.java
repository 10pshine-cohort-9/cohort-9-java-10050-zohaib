package com.contactmanagement.controller;

import com.contactmanagement.dto.*;
import com.contactmanagement.exception.ResourceNotFoundException;
import com.contactmanagement.model.User;
import com.contactmanagement.repository.UserRepository;
import com.contactmanagement.security.JwtAuthenticationFilter;
import com.contactmanagement.security.JwtTokenProvider;
import com.contactmanagement.service.ContactService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = ContactController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class
    )
)
@DisplayName("ContactController")
class ContactControllerTest {

    @Autowired MockMvc mockMvc;

    @MockBean ContactService     contactService;
    @MockBean UserRepository     userRepository;
    @MockBean JwtTokenProvider   jwtTokenProvider;
    @MockBean UserDetailsService userDetailsService;

    private ObjectMapper objectMapper;

    private static final Long USER_ID    = 1L;
    private static final Long CONTACT_ID = 10L;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        User mockUser = User.builder()
                .id(USER_ID)
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .password("hash")
                .build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(userRepository.findByPhone(anyString())).thenReturn(Optional.empty());
    }

    private ContactResponse sampleContactResponse() {
        return ContactResponse.builder()
                .id(CONTACT_ID)
                .firstName("John")
                .lastName("Doe")
                .title("Engineer")
                .company("Acme")
                .phones(List.of(ContactPhoneDto.builder().id(1L).label("mobile").number("+123456789").build()))
                .emails(List.of(ContactEmailDto.builder().id(1L).label("work").address("john@example.com").build()))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private PagedResponse<ContactSummaryResponse> samplePage() {
        ContactSummaryResponse summary = ContactSummaryResponse.builder()
                .id(CONTACT_ID)
                .firstName("John")
                .lastName("Doe")
                .title("Engineer")
                .primaryPhone("+123456789")
                .primaryEmail("john@example.com")
                .build();
        return PagedResponse.<ContactSummaryResponse>builder()
                .content(List.of(summary))
                .page(0).size(10).totalElements(1).totalPages(1).last(true)
                .build();
    }

    @Nested
    @DisplayName("GET /api/contacts")
    class GetContacts {

        @Test
        @DisplayName("returns 200 paged list for authenticated user")
        @WithMockUser(username = "test@example.com", roles = "USER")
        void getContacts_returns200() throws Exception {
            when(contactService.getContacts(eq(USER_ID), any(Pageable.class)))
                    .thenReturn(samplePage());

            mockMvc.perform(get("/api/contacts").param("page", "0").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.content[0].firstName").value("John"))
                    .andExpect(jsonPath("$.content[0].primaryPhone").value("+123456789"));
        }

        @Test
        @DisplayName("delegates to searchContacts when search param is present")
        @WithMockUser(username = "test@example.com", roles = "USER")
        void getContacts_withSearch_delegatesToSearch() throws Exception {
            when(contactService.searchContacts(eq(USER_ID), eq("John"), any(Pageable.class)))
                    .thenReturn(samplePage());

            mockMvc.perform(get("/api/contacts").param("search", "John"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].firstName").value("John"));

            verify(contactService).searchContacts(eq(USER_ID), eq("John"), any(Pageable.class));
            verify(contactService, never()).getContacts(any(), any());
        }

        @Test
        @DisplayName("returns 401 when not authenticated")
        void getContacts_unauthenticated_returns401() throws Exception {
            mockMvc.perform(get("/api/contacts"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/contacts/{id}")
    class GetContactById {

        @Test
        @DisplayName("returns 200 with full contact detail")
        @WithMockUser(username = "test@example.com", roles = "USER")
        void getContact_returns200() throws Exception {
            when(contactService.getContactById(USER_ID, CONTACT_ID))
                    .thenReturn(sampleContactResponse());

            mockMvc.perform(get("/api/contacts/{id}", CONTACT_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(CONTACT_ID))
                    .andExpect(jsonPath("$.firstName").value("John"))
                    .andExpect(jsonPath("$.phones[0].label").value("mobile"))
                    .andExpect(jsonPath("$.emails[0].label").value("work"));
        }

        @Test
        @DisplayName("returns 404 when contact not found")
        @WithMockUser(username = "test@example.com", roles = "USER")
        void getContact_notFound_returns404() throws Exception {
            when(contactService.getContactById(USER_ID, CONTACT_ID))
                    .thenThrow(new ResourceNotFoundException("Contact", "id", CONTACT_ID));

            mockMvc.perform(get("/api/contacts/{id}", CONTACT_ID))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/contacts")
    class CreateContact {

        @Test
        @DisplayName("returns 201 with created contact")
        @WithMockUser(username = "test@example.com", roles = "USER")
        void createContact_returns201() throws Exception {
            CreateContactRequest req = new CreateContactRequest();
            req.setFirstName("John");
            req.setLastName("Doe");

            when(contactService.createContact(eq(USER_ID), any(CreateContactRequest.class)))
                    .thenReturn(sampleContactResponse());

            mockMvc.perform(post("/api/contacts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req))
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(CONTACT_ID))
                    .andExpect(jsonPath("$.firstName").value("John"));
        }

        @Test
        @DisplayName("returns 400 when firstName is blank")
        @WithMockUser(username = "test@example.com", roles = "USER")
        void createContact_blankFirstName_returns400() throws Exception {
            CreateContactRequest req = new CreateContactRequest();
            req.setFirstName("");
            req.setLastName("Doe");

            mockMvc.perform(post("/api/contacts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req))
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.firstName").exists());
        }
    }

    @Nested
    @DisplayName("PUT /api/contacts/{id}")
    class UpdateContact {

        @Test
        @DisplayName("returns 200 with updated contact")
        @WithMockUser(username = "test@example.com", roles = "USER")
        void updateContact_returns200() throws Exception {
            UpdateContactRequest req = new UpdateContactRequest();
            req.setFirstName("Jane");
            req.setLastName("Updated");

            ContactResponse updated = sampleContactResponse();
            updated.setFirstName("Jane");

            when(contactService.updateContact(eq(USER_ID), eq(CONTACT_ID), any(UpdateContactRequest.class)))
                    .thenReturn(updated);

            mockMvc.perform(put("/api/contacts/{id}", CONTACT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.firstName").value("Jane"));
        }

        @Test
        @DisplayName("returns 404 when contact not found")
        @WithMockUser(username = "test@example.com", roles = "USER")
        void updateContact_notFound_returns404() throws Exception {
            UpdateContactRequest req = new UpdateContactRequest();
            req.setFirstName("Jane");
            req.setLastName("Updated");

            when(contactService.updateContact(eq(USER_ID), eq(CONTACT_ID), any()))
                    .thenThrow(new ResourceNotFoundException("Contact", "id", CONTACT_ID));

            mockMvc.perform(put("/api/contacts/{id}", CONTACT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req))
                            .with(csrf()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/contacts/{id}")
    class DeleteContact {

        @Test
        @DisplayName("returns 200 with success message")
        @WithMockUser(username = "test@example.com", roles = "USER")
        void deleteContact_returns200() throws Exception {
            doNothing().when(contactService).deleteContact(USER_ID, CONTACT_ID);

            mockMvc.perform(delete("/api/contacts/{id}", CONTACT_ID).with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Contact deleted successfully."));
        }

        @Test
        @DisplayName("returns 404 when contact not found")
        @WithMockUser(username = "test@example.com", roles = "USER")
        void deleteContact_notFound_returns404() throws Exception {
            doThrow(new ResourceNotFoundException("Contact", "id", CONTACT_ID))
                    .when(contactService).deleteContact(USER_ID, CONTACT_ID);

            mockMvc.perform(delete("/api/contacts/{id}", CONTACT_ID).with(csrf()))
                    .andExpect(status().isNotFound());
        }
    }
}
