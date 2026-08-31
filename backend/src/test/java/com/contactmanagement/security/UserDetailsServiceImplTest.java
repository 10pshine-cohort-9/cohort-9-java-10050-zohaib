package com.contactmanagement.security;

import com.contactmanagement.model.User;
import com.contactmanagement.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserDetailsServiceImpl")
class UserDetailsServiceImplTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserDetailsServiceImpl userDetailsService;

    private User activeEmailUser() {
        return User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("$2a$12$hashedPassword")
                .role("ROLE_USER")
                .isActive(true)
                .build();
    }

    private User activePhoneUser() {
        return User.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .phone("+923001234567")
                .password("$2a$12$hashedPassword")
                .role("ROLE_USER")
                .isActive(true)
                .build();
    }

    @Nested
    @DisplayName("loadUserByUsername() — email identifier")
    class EmailIdentifier {

        @Test
        @DisplayName("routes to findByEmail when identifier contains '@'")
        void loadByEmail_callsFindByEmail() {
            when(userRepository.findByEmail("john@example.com"))
                    .thenReturn(Optional.of(activeEmailUser()));

            userDetailsService.loadUserByUsername("john@example.com");

            verify(userRepository).findByEmail("john@example.com");
        }

        @Test
        @DisplayName("returns UserDetails with correct username (email)")
        void loadByEmail_usernameIsEmail() {
            when(userRepository.findByEmail("john@example.com"))
                    .thenReturn(Optional.of(activeEmailUser()));

            UserDetails result = userDetailsService.loadUserByUsername("john@example.com");

            assertThat(result.getUsername()).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("returns UserDetails with the stored password hash")
        void loadByEmail_passwordMatches() {
            when(userRepository.findByEmail("john@example.com"))
                    .thenReturn(Optional.of(activeEmailUser()));

            UserDetails result = userDetailsService.loadUserByUsername("john@example.com");

            assertThat(result.getPassword()).isEqualTo("$2a$12$hashedPassword");
        }

        @Test
        @DisplayName("returns UserDetails with the user's role as a GrantedAuthority")
        void loadByEmail_hasCorrectAuthority() {
            when(userRepository.findByEmail("john@example.com"))
                    .thenReturn(Optional.of(activeEmailUser()));

            UserDetails result = userDetailsService.loadUserByUsername("john@example.com");

            assertThat(result.getAuthorities())
                    .extracting("authority")
                    .containsExactly("ROLE_USER");
        }

        @Test
        @DisplayName("throws UsernameNotFoundException when email not found")
        void loadByEmail_notFound_throwsUsernameNotFoundException() {
            when(userRepository.findByEmail("ghost@example.com"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> userDetailsService.loadUserByUsername("ghost@example.com"))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining("ghost@example.com");
        }
    }

    @Nested
    @DisplayName("loadUserByUsername() — phone identifier")
    class PhoneIdentifier {

        @Test
        @DisplayName("routes to findByPhone when identifier has no '@'")
        void loadByPhone_callsFindByPhone() {
            when(userRepository.findByPhone("+923001234567"))
                    .thenReturn(Optional.of(activePhoneUser()));

            userDetailsService.loadUserByUsername("+923001234567");

            verify(userRepository).findByPhone("+923001234567");
        }

        @Test
        @DisplayName("returns UserDetails with phone as username when user has no email")
        void loadByPhone_usernameIsPhone() {
            when(userRepository.findByPhone("+923001234567"))
                    .thenReturn(Optional.of(activePhoneUser()));

            UserDetails result = userDetailsService.loadUserByUsername("+923001234567");

            assertThat(result.getUsername()).isEqualTo("+923001234567");
        }

        @Test
        @DisplayName("throws UsernameNotFoundException when phone not found")
        void loadByPhone_notFound_throwsUsernameNotFoundException() {
            when(userRepository.findByPhone("+0000000000"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> userDetailsService.loadUserByUsername("+0000000000"))
                    .isInstanceOf(UsernameNotFoundException.class)
                    .hasMessageContaining("+0000000000");
        }
    }

    @Nested
    @DisplayName("loadUserByUsername() — active / disabled state")
    class AccountState {

        @Test
        @DisplayName("isEnabled() returns true for an active user")
        void activeUser_isEnabled() {
            when(userRepository.findByEmail("active@example.com"))
                    .thenReturn(Optional.of(activeEmailUser()));

            UserDetails result = userDetailsService.loadUserByUsername("active@example.com");

            assertThat(result.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("isEnabled() returns false for a disabled user")
        void disabledUser_isNotEnabled() {
            User disabled = User.builder()
                    .id(3L)
                    .firstName("Locked")
                    .lastName("Account")
                    .email("locked@example.com")
                    .password("$2a$12$hash")
                    .role("ROLE_USER")
                    .isActive(false)
                    .build();
            when(userRepository.findByEmail("locked@example.com"))
                    .thenReturn(Optional.of(disabled));

            UserDetails result = userDetailsService.loadUserByUsername("locked@example.com");

            assertThat(result.isEnabled()).isFalse();
        }

        @Test
        @DisplayName("admin role is mapped as ROLE_ADMIN authority")
        void adminUser_hasAdminAuthority() {
            User admin = User.builder()
                    .id(4L)
                    .firstName("Admin")
                    .lastName("User")
                    .email("admin@example.com")
                    .password("$2a$12$hash")
                    .role("ROLE_ADMIN")
                    .isActive(true)
                    .build();
            when(userRepository.findByEmail("admin@example.com"))
                    .thenReturn(Optional.of(admin));

            UserDetails result = userDetailsService.loadUserByUsername("admin@example.com");

            assertThat(result.getAuthorities())
                    .extracting("authority")
                    .containsExactly("ROLE_ADMIN");
        }

        @Test
        @DisplayName("prefers email over phone as principal when user has both")
        void userWithBothEmailAndPhone_usesEmailAsPrincipal() {
            User both = User.builder()
                    .id(5L)
                    .firstName("Both")
                    .lastName("Fields")
                    .email("both@example.com")
                    .phone("+923001234567")
                    .password("$2a$12$hash")
                    .role("ROLE_USER")
                    .isActive(true)
                    .build();
            when(userRepository.findByEmail("both@example.com"))
                    .thenReturn(Optional.of(both));

            UserDetails result = userDetailsService.loadUserByUsername("both@example.com");

            assertThat(result.getUsername()).isEqualTo("both@example.com");
        }
    }
}
