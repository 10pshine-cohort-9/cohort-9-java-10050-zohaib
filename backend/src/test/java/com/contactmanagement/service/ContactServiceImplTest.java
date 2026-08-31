package com.contactmanagement.service;

import com.contactmanagement.dto.*;
import com.contactmanagement.exception.ResourceNotFoundException;
import com.contactmanagement.model.Contact;
import com.contactmanagement.model.ContactEmail;
import com.contactmanagement.model.ContactPhone;
import com.contactmanagement.repository.ContactRepository;
import com.contactmanagement.service.impl.ContactServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContactServiceImpl")
class ContactServiceImplTest {

    @Mock ContactRepository contactRepository;
    @InjectMocks ContactServiceImpl contactService;

    private static final Long USER_ID    = 1L;
    private static final Long CONTACT_ID = 10L;

    private Contact sampleContact() {
        ContactPhone phone = ContactPhone.builder().id(1L).label("mobile").number("+123456789").build();
        ContactEmail email = ContactEmail.builder().id(1L).label("work").address("john@example.com").build();

        Contact c = Contact.builder()
                .id(CONTACT_ID)
                .userId(USER_ID)
                .firstName("John")
                .lastName("Doe")
                .title("Engineer")
                .company("Acme")
                .address("123 Main St")
                .notes("Test note")
                .createdAt(LocalDateTime.now())
                .build();
        c.getPhones().add(phone);
        c.getEmails().add(email);
        phone.setContact(c);
        email.setContact(c);
        return c;
    }

    private CreateContactRequest sampleCreateRequest() {
        CreateContactRequest req = new CreateContactRequest();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setTitle("Engineer");
        req.setCompany("Acme");
        ContactPhoneDto phone = ContactPhoneDto.builder().label("mobile").number("+123456789").build();
        ContactEmailDto email = ContactEmailDto.builder().label("work").address("john@example.com").build();
        req.setPhones(new ArrayList<>(List.of(phone)));
        req.setEmails(new ArrayList<>(List.of(email)));
        return req;
    }

    @Nested
    @DisplayName("getContacts()")
    class GetContacts {

        @Test
        @DisplayName("returns paged summary for the user")
        void getContacts_returnsPagedSummary() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Contact> page = new PageImpl<>(List.of(sampleContact()), pageable, 1);
            when(contactRepository.findByUserId(USER_ID, pageable)).thenReturn(page);

            PagedResponse<ContactSummaryResponse> result = contactService.getContacts(USER_ID, pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getFirstName()).isEqualTo("John");
            assertThat(result.getContent().get(0).getPrimaryPhone()).isEqualTo("+123456789");
            assertThat(result.getContent().get(0).getPrimaryEmail()).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("returns empty page when user has no contacts")
        void getContacts_emptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            when(contactRepository.findByUserId(USER_ID, pageable))
                    .thenReturn(new PageImpl<>(List.of(), pageable, 0));

            PagedResponse<ContactSummaryResponse> result = contactService.getContacts(USER_ID, pageable);

            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("searchContacts()")
    class SearchContacts {

        @Test
        @DisplayName("delegates to repository with trimmed query")
        void searchContacts_delegatesToRepository() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Contact> page = new PageImpl<>(List.of(sampleContact()), pageable, 1);
            when(contactRepository.searchByUserIdAndQuery(eq(USER_ID), eq("John"), eq(pageable)))
                    .thenReturn(page);

            PagedResponse<ContactSummaryResponse> result =
                    contactService.searchContacts(USER_ID, " John ", pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(contactRepository).searchByUserIdAndQuery(USER_ID, "John", pageable);
        }
    }

    @Nested
    @DisplayName("getContactById()")
    class GetContactById {

        @Test
        @DisplayName("returns full ContactResponse when contact belongs to user")
        void getContactById_success() {
            when(contactRepository.findById(CONTACT_ID)).thenReturn(Optional.of(sampleContact()));

            ContactResponse result = contactService.getContactById(USER_ID, CONTACT_ID);

            assertThat(result.getId()).isEqualTo(CONTACT_ID);
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getPhones()).hasSize(1);
            assertThat(result.getEmails()).hasSize(1);
            assertThat(result.getPhones().get(0).getLabel()).isEqualTo("mobile");
            assertThat(result.getEmails().get(0).getLabel()).isEqualTo("work");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when contact does not exist")
        void getContactById_notFound() {
            when(contactRepository.findById(CONTACT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contactService.getContactById(USER_ID, CONTACT_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when contact belongs to another user")
        void getContactById_wrongUser() {
            Contact contact = sampleContact();
            contact.setUserId(999L);
            when(contactRepository.findById(CONTACT_ID)).thenReturn(Optional.of(contact));

            assertThatThrownBy(() -> contactService.getContactById(USER_ID, CONTACT_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("createContact()")
    class CreateContact {

        @Test
        @DisplayName("saves contact and returns ContactResponse")
        void createContact_success() {
            CreateContactRequest req = sampleCreateRequest();
            when(contactRepository.save(any(Contact.class))).thenAnswer(inv -> {
                Contact c = inv.getArgument(0);
                c.setId(CONTACT_ID);
                c.setCreatedAt(LocalDateTime.now());
                return c;
            });

            ContactResponse result = contactService.createContact(USER_ID, req);

            assertThat(result.getId()).isEqualTo(CONTACT_ID);
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getPhones()).hasSize(1);
            assertThat(result.getEmails()).hasSize(1);
            verify(contactRepository).save(any(Contact.class));
        }

        @Test
        @DisplayName("stores email address in lowercase")
        void createContact_emailLowercase() {
            CreateContactRequest req = sampleCreateRequest();
            req.getEmails().get(0).setAddress("JOHN@EXAMPLE.COM");

            when(contactRepository.save(any(Contact.class))).thenAnswer(inv -> {
                Contact c = inv.getArgument(0);
                c.setId(CONTACT_ID);
                return c;
            });

            ContactResponse result = contactService.createContact(USER_ID, req);

            assertThat(result.getEmails().get(0).getAddress()).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("creates contact with no phones or emails")
        void createContact_noPhoneOrEmail() {
            CreateContactRequest req = new CreateContactRequest();
            req.setFirstName("Jane");
            req.setLastName("Smith");

            when(contactRepository.save(any(Contact.class))).thenAnswer(inv -> {
                Contact c = inv.getArgument(0);
                c.setId(CONTACT_ID);
                return c;
            });

            ContactResponse result = contactService.createContact(USER_ID, req);

            assertThat(result.getPhones()).isEmpty();
            assertThat(result.getEmails()).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateContact()")
    class UpdateContact {

        @Test
        @DisplayName("updates fields and replaces phones/emails")
        void updateContact_success() {
            Contact existing = sampleContact();
            when(contactRepository.findById(CONTACT_ID)).thenReturn(Optional.of(existing));
            when(contactRepository.save(any(Contact.class))).thenAnswer(inv -> inv.getArgument(0));

            UpdateContactRequest req = new UpdateContactRequest();
            req.setFirstName("Jane");
            req.setLastName("Updated");
            req.setTitle("Manager");
            ContactPhoneDto newPhone = ContactPhoneDto.builder().label("home").number("+987654321").build();
            req.setPhones(new ArrayList<>(List.of(newPhone)));
            req.setEmails(new ArrayList<>());

            ContactResponse result = contactService.updateContact(USER_ID, CONTACT_ID, req);

            assertThat(result.getFirstName()).isEqualTo("Jane");
            assertThat(result.getTitle()).isEqualTo("Manager");
            assertThat(result.getPhones()).hasSize(1);
            assertThat(result.getPhones().get(0).getNumber()).isEqualTo("+987654321");
            assertThat(result.getEmails()).isEmpty();
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when contact not found")
        void updateContact_notFound() {
            when(contactRepository.findById(CONTACT_ID)).thenReturn(Optional.empty());

            UpdateContactRequest req = new UpdateContactRequest();
            req.setFirstName("Jane");
            req.setLastName("Smith");

            assertThatThrownBy(() -> contactService.updateContact(USER_ID, CONTACT_ID, req))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteContact()")
    class DeleteContact {

        @Test
        @DisplayName("sets isDeleted=true and saves")
        void deleteContact_softDelete() {
            Contact contact = sampleContact();
            when(contactRepository.findById(CONTACT_ID)).thenReturn(Optional.of(contact));
            when(contactRepository.save(any(Contact.class))).thenAnswer(inv -> inv.getArgument(0));

            contactService.deleteContact(USER_ID, CONTACT_ID);

            ArgumentCaptor<Contact> captor = ArgumentCaptor.forClass(Contact.class);
            verify(contactRepository).save(captor.capture());
            assertThat(captor.getValue().isDeleted()).isTrue();
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when contact not found")
        void deleteContact_notFound() {
            when(contactRepository.findById(CONTACT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> contactService.deleteContact(USER_ID, CONTACT_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when contact belongs to another user")
        void deleteContact_wrongUser() {
            Contact contact = sampleContact();
            contact.setUserId(999L);
            when(contactRepository.findById(CONTACT_ID)).thenReturn(Optional.of(contact));

            assertThatThrownBy(() -> contactService.deleteContact(USER_ID, CONTACT_ID))
                    .isInstanceOf(ResourceNotFoundException.class);
            verify(contactRepository, never()).save(any());
        }
    }
}
