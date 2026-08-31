package com.contactmanagement.repository;

import com.contactmanagement.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository")
class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    private User emailUser;
    private User phoneUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        emailUser = userRepository.save(User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("$2a$12$hash1")
                .build());

        phoneUser = userRepository.save(User.builder()
                .firstName("Jane")
                .lastName("Smith")
                .phone("+923001234567")
                .password("$2a$12$hash2")
                .build());
    }

    @Nested
    @DisplayName("findByEmail()")
    class FindByEmail {

        @Test
        @DisplayName("returns user when email matches")
        void findByEmail_existingEmail_returnsUser() {
            Optional<User> result = userRepository.findByEmail("john@example.com");

            assertThat(result).isPresent();
            assertThat(result.get().getFirstName()).isEqualTo("John");
            assertThat(result.get().getEmail()).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("returns empty when email does not exist")
        void findByEmail_unknownEmail_returnsEmpty() {
            Optional<User> result = userRepository.findByEmail("nobody@example.com");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("is case-sensitive — different case returns empty")
        void findByEmail_wrongCase_returnsEmpty() {
            Optional<User> result = userRepository.findByEmail("JOHN@EXAMPLE.COM");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("does not return user that only has a phone number")
        void findByEmail_phoneOnlyUser_returnsEmpty() {
            Optional<User> result = userRepository.findByEmail("+923001234567");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByPhone()")
    class FindByPhone {

        @Test
        @DisplayName("returns user when phone matches")
        void findByPhone_existingPhone_returnsUser() {
            Optional<User> result = userRepository.findByPhone("+923001234567");

            assertThat(result).isPresent();
            assertThat(result.get().getFirstName()).isEqualTo("Jane");
            assertThat(result.get().getPhone()).isEqualTo("+923001234567");
        }

        @Test
        @DisplayName("returns empty when phone does not exist")
        void findByPhone_unknownPhone_returnsEmpty() {
            Optional<User> result = userRepository.findByPhone("+0000000000");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("does not return user that only has an email address")
        void findByPhone_emailOnlyUser_returnsEmpty() {
            Optional<User> result = userRepository.findByPhone("john@example.com");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByEmail()")
    class ExistsByEmail {

        @Test
        @DisplayName("returns true when email is registered")
        void existsByEmail_registered_returnsTrue() {
            assertThat(userRepository.existsByEmail("john@example.com")).isTrue();
        }

        @Test
        @DisplayName("returns false when email is not registered")
        void existsByEmail_notRegistered_returnsFalse() {
            assertThat(userRepository.existsByEmail("new@example.com")).isFalse();
        }
    }

    @Nested
    @DisplayName("existsByPhone()")
    class ExistsByPhone {

        @Test
        @DisplayName("returns true when phone is registered")
        void existsByPhone_registered_returnsTrue() {
            assertThat(userRepository.existsByPhone("+923001234567")).isTrue();
        }

        @Test
        @DisplayName("returns false when phone is not registered")
        void existsByPhone_notRegistered_returnsFalse() {
            assertThat(userRepository.existsByPhone("+0000000000")).isFalse();
        }
    }

    @Nested
    @DisplayName("save() — constraints")
    class SaveConstraints {

        @Test
        @DisplayName("persists user and auto-generates id")
        void save_persistsWithGeneratedId() {
            User saved = userRepository.save(User.builder()
                    .firstName("New")
                    .lastName("User")
                    .email("new@example.com")
                    .password("$2a$12$hash3")
                    .build());

            assertThat(saved.getId()).isNotNull().isPositive();
        }

        @Test
        @DisplayName("default role is ROLE_USER when not specified")
        void save_defaultRoleIsUser() {
            User saved = userRepository.save(User.builder()
                    .firstName("Role")
                    .lastName("Test")
                    .email("role@example.com")
                    .password("$2a$12$hash4")
                    .build());

            assertThat(saved.getRole()).isEqualTo("ROLE_USER");
        }

        @Test
        @DisplayName("default isActive is true when not specified")
        void save_defaultIsActiveIsTrue() {
            User saved = userRepository.save(User.builder()
                    .firstName("Active")
                    .lastName("Test")
                    .email("active@example.com")
                    .password("$2a$12$hash5")
                    .build());

            assertThat(saved.isActive()).isTrue();
        }

        @Test
        @DisplayName("findById returns the saved user")
        void findById_returnsSavedUser() {
            Optional<User> found = userRepository.findById(emailUser.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getEmail()).isEqualTo("john@example.com");
        }
    }
}
