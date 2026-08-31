package com.contactmanagement.repository;

import com.contactmanagement.model.Contact;
import com.contactmanagement.model.ContactEmail;
import com.contactmanagement.model.ContactPhone;
import com.contactmanagement.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("ContactRepository")
class ContactRepositoryTest {

    @Autowired ContactRepository contactRepository;
    @Autowired UserRepository    userRepository;

    private static final Long OTHER_USER_ID = 99L;

    private User owner;
    private Contact alice;
    private Contact bob;
    private Contact charlie;

    @BeforeEach
    void setUp() {
        contactRepository.deleteAll();
        userRepository.deleteAll();

        owner = userRepository.save(User.builder()
                .firstName("Owner")
                .lastName("User")
                .email("owner@example.com")
                .password("$2a$12$hash")
                .build());

        alice = buildAndSave("Alice", "Johnson", "Engineer",  "Acme Corp",
                "alice@work.com", "work", "+111111111", "mobile", owner.getId(), false);

        bob = buildAndSave("Bob", "Williams", "Designer", "Globex",
                "bob@personal.com", "personal", "+222222222", "home", owner.getId(), false);

        charlie = buildAndSave("Charlie", "Brown", "Manager", "Initech",
                "charlie@work.com", "work", "+333333333", "work", owner.getId(), false);
    }

    private Contact buildAndSave(String firstName, String lastName,
                                  String title, String company,
                                  String emailAddress, String emailLabel,
                                  String phoneNumber, String phoneLabel,
                                  Long userId, boolean deleted) {
        Contact contact = Contact.builder()
                .userId(userId)
                .firstName(firstName)
                .lastName(lastName)
                .title(title)
                .company(company)
                .isDeleted(deleted)
                .build();

        ContactEmail email = ContactEmail.builder()
                .label(emailLabel)
                .address(emailAddress)
                .build();
        ContactPhone phone = ContactPhone.builder()
                .label(phoneLabel)
                .number(phoneNumber)
                .build();

        contact.addEmail(email);
        contact.addPhone(phone);

        return contactRepository.save(contact);
    }

    @Nested
    @DisplayName("findByUserId()")
    class FindByUserId {

        @Test
        @DisplayName("returns all contacts belonging to the given user")
        void findByUserId_returnsOwnerContacts() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Contact> page = contactRepository.findByUserId(owner.getId(), pageable);

            assertThat(page.getTotalElements()).isEqualTo(3);
            assertThat(page.getContent())
                    .extracting(Contact::getFirstName)
                    .containsExactlyInAnyOrder("Alice", "Bob", "Charlie");
        }

        @Test
        @DisplayName("returns empty page when user has no contacts")
        void findByUserId_unknownUser_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Contact> page = contactRepository.findByUserId(OTHER_USER_ID, pageable);

            assertThat(page.getTotalElements()).isZero();
            assertThat(page.getContent()).isEmpty();
        }

        @Test
        @DisplayName("pagination limits the number of returned contacts")
        void findByUserId_pagination_limitsResults() {
            Pageable firstPage  = PageRequest.of(0, 2, Sort.by("lastName").ascending());
            Pageable secondPage = PageRequest.of(1, 2, Sort.by("lastName").ascending());

            Page<Contact> page1 = contactRepository.findByUserId(owner.getId(), firstPage);
            Page<Contact> page2 = contactRepository.findByUserId(owner.getId(), secondPage);

            assertThat(page1.getContent()).hasSize(2);
            assertThat(page2.getContent()).hasSize(1);
            assertThat(page1.getTotalElements()).isEqualTo(3);
            assertThat(page1.getTotalPages()).isEqualTo(2);
        }

        @Test
        @DisplayName("does not return contacts belonging to a different user")
        void findByUserId_doesNotLeakOtherUsersContacts() {
            User otherUser = userRepository.save(User.builder()
                    .firstName("Other")
                    .lastName("User")
                    .email("other@example.com")
                    .password("$2a$12$hash2")
                    .build());

            buildAndSave("Dave", "Other", "Other", "Other Co",
                    "dave@other.com", "work", "+444444444", "mobile",
                    otherUser.getId(), false);

            Pageable pageable = PageRequest.of(0, 10);
            Page<Contact> page = contactRepository.findByUserId(owner.getId(), pageable);

            assertThat(page.getTotalElements()).isEqualTo(3);
            assertThat(page.getContent())
                    .extracting(Contact::getFirstName)
                    .doesNotContain("Dave");
        }
    }

    @Nested
    @DisplayName("searchByUserIdAndQuery()")
    class SearchByUserIdAndQuery {

        @Test
        @DisplayName("finds contacts by first name (case-insensitive)")
        void search_byFirstName_caseInsensitive() {
            Page<Contact> result = contactRepository.searchByUserIdAndQuery(
                    owner.getId(), "alice", PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getFirstName()).isEqualTo("Alice");
        }

        @Test
        @DisplayName("finds contacts by last name (case-insensitive)")
        void search_byLastName_caseInsensitive() {
            Page<Contact> result = contactRepository.searchByUserIdAndQuery(
                    owner.getId(), "WILLIAMS", PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getLastName()).isEqualTo("Williams");
        }

        @Test
        @DisplayName("finds contacts by company name")
        void search_byCompany() {
            Page<Contact> result = contactRepository.searchByUserIdAndQuery(
                    owner.getId(), "Acme", PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getCompany()).isEqualTo("Acme Corp");
        }

        @Test
        @DisplayName("finds contacts by phone number")
        void search_byPhoneNumber() {
            Page<Contact> result = contactRepository.searchByUserIdAndQuery(
                    owner.getId(), "+222222222", PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getFirstName()).isEqualTo("Bob");
        }

        @Test
        @DisplayName("finds contacts by email address")
        void search_byEmailAddress() {
            Page<Contact> result = contactRepository.searchByUserIdAndQuery(
                    owner.getId(), "charlie@work.com", PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getFirstName()).isEqualTo("Charlie");
        }

        @Test
        @DisplayName("returns multiple contacts when query matches more than one")
        void search_matchingMultiple_returnsAll() {
            Page<Contact> result = contactRepository.searchByUserIdAndQuery(
                    owner.getId(), "work", PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isGreaterThanOrEqualTo(2);
        }

        @Test
        @DisplayName("returns empty page when query matches nothing")
        void search_noMatch_returnsEmpty() {
            Page<Contact> result = contactRepository.searchByUserIdAndQuery(
                    owner.getId(), "zzznomatch", PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("does not return contacts belonging to another user")
        void search_doesNotLeakOtherUsersContacts() {
            Page<Contact> result = contactRepository.searchByUserIdAndQuery(
                    OTHER_USER_ID, "Alice", PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("partial match works — finds by name fragment")
        void search_partialMatch_findsContact() {
            Page<Contact> result = contactRepository.searchByUserIdAndQuery(
                    owner.getId(), "Joh", PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getLastName()).isEqualTo("Johnson");
        }
    }

    @Nested
    @DisplayName("@Where soft-delete filter")
    class SoftDeleteFilter {

        @Test
        @DisplayName("findByUserId excludes soft-deleted contacts")
        void findByUserId_excludesSoftDeleted() {
            buildAndSave("Deleted", "Contact", "Old", "OldCo",
                    "deleted@example.com", "work", "+999999999", "mobile",
                    owner.getId(), true);

            Page<Contact> page = contactRepository.findByUserId(
                    owner.getId(), PageRequest.of(0, 10));

            assertThat(page.getTotalElements()).isEqualTo(3);
            assertThat(page.getContent())
                    .extracting(Contact::getFirstName)
                    .doesNotContain("Deleted");
        }

        @Test
        @DisplayName("searchByUserIdAndQuery excludes soft-deleted contacts")
        void search_excludesSoftDeleted() {
            buildAndSave("Ghosted", "Contact", "Ghost", "GhostCo",
                    "ghost@example.com", "work", "+888888888", "mobile",
                    owner.getId(), true);

            Page<Contact> result = contactRepository.searchByUserIdAndQuery(
                    owner.getId(), "Ghosted", PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("soft-deleting a contact hides it from subsequent findByUserId")
        void softDelete_thenFind_contactIsHidden() {
            alice.setDeleted(true);
            contactRepository.save(alice);

            Page<Contact> page = contactRepository.findByUserId(
                    owner.getId(), PageRequest.of(0, 10));

            assertThat(page.getTotalElements()).isEqualTo(2);
            assertThat(page.getContent())
                    .extracting(Contact::getFirstName)
                    .doesNotContain("Alice");
        }
    }

    @Nested
    @DisplayName("existsByIdAndUserId()")
    class ExistsByIdAndUserId {

        @Test
        @DisplayName("returns true when contact belongs to user")
        void existsByIdAndUserId_owned_returnsTrue() {
            assertThat(contactRepository.existsByIdAndUserId(alice.getId(), owner.getId()))
                    .isTrue();
        }

        @Test
        @DisplayName("returns false when contact belongs to another user")
        void existsByIdAndUserId_wrongUser_returnsFalse() {
            assertThat(contactRepository.existsByIdAndUserId(alice.getId(), OTHER_USER_ID))
                    .isFalse();
        }

        @Test
        @DisplayName("returns false when contact id does not exist")
        void existsByIdAndUserId_unknownId_returnsFalse() {
            assertThat(contactRepository.existsByIdAndUserId(99999L, owner.getId()))
                    .isFalse();
        }
    }
}
