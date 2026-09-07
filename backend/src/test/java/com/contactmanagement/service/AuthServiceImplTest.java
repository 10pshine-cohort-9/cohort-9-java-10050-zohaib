package com.contactmanagement.service;

import com.contactmanagement.dto.*;
import com.contactmanagement.exception.BadRequestException;
import com.contactmanagement.exception.InvalidCredentialsException;
import com.contactmanagement.exception.UserAlreadyExistsException;
import com.contactmanagement.model.User;
import com.contactmanagement.repository.UserRepository;
import com.contactmanagement.security.JwtTokenProvider;
import com.contactmanagement.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl")
class AuthServiceImplTest {

    @Mock UserRepository        userRepository;
    @Mock PasswordEncoder       passwordEncoder;
    @Mock JwtTokenProvider      jwtTokenProvider;
    @Mock AuthenticationManager authenticationManager;

    @InjectMocks AuthServiceImpl authService;

    private RegisterRequest emailRegisterRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setEmail("john@example.com");
        req.setPassword("Password123!");
        return req;
    }

    private RegisterRequest phoneRegisterRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("Jane");
        req.setLastName("Smith");
        req.setPhone("+923001234567");
        req.setPassword("Password123!");
        return req;
    }

    private User savedUser() {
        return User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .password("$2a$12$encodedPassword")
                .role("ROLE_USER")
                .isActive(true)
                .build();
    }

    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("registers successfully with email")
        void registerWithEmail_success() {
            RegisterRequest req = emailRegisterRequest();
            when(userRepository.existsByEmail(req.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(req.getPassword())).thenReturn("$2a$12$hash");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                u = User.builder()
                        .id(1L).firstName(u.getFirstName()).lastName(u.getLastName())
                        .email(u.getEmail()).password(u.getPassword()).role("ROLE_USER").build();
                return u;
            });
            when(jwtTokenProvider.generateTokenFromUsername("john@example.com"))
                    .thenReturn("jwt-token");

            AuthResponse response = authService.register(req);

            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getEmail()).isEqualTo("john@example.com");
            assertThat(response.getUserId()).isEqualTo(1L);
            assertThat(response.getRole()).isEqualTo("ROLE_USER");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("registers successfully with phone only")
        void registerWithPhone_success() {
            RegisterRequest req = phoneRegisterRequest();
            when(userRepository.existsByPhone(req.getPhone())).thenReturn(false);
            when(passwordEncoder.encode(req.getPassword())).thenReturn("$2a$12$hash");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                return User.builder()
                        .id(2L).firstName(u.getFirstName()).lastName(u.getLastName())
                        .phone(u.getPhone()).password(u.getPassword()).role("ROLE_USER").build();
            });
            when(jwtTokenProvider.generateTokenFromUsername(req.getPhone()))
                    .thenReturn("jwt-phone-token");

            AuthResponse response = authService.register(req);

            assertThat(response.getToken()).isEqualTo("jwt-phone-token");
            assertThat(response.getPhone()).isEqualTo(req.getPhone());
        }

        @Test
        @DisplayName("throws BadRequestException when neither email nor phone provided")
        void register_noIdentifier_throwsBadRequest() {
            RegisterRequest req = new RegisterRequest();
            req.setFirstName("No");
            req.setLastName("Id");
            req.setPassword("Password123!");

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("At least one of email or phone");
        }

        @Test
        @DisplayName("throws UserAlreadyExistsException when email is taken")
        void register_duplicateEmail_throwsConflict() {
            RegisterRequest req = emailRegisterRequest();
            when(userRepository.existsByEmail(req.getEmail())).thenReturn(true);

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .hasMessageContaining(req.getEmail());
        }

        @Test
        @DisplayName("throws UserAlreadyExistsException when phone is taken")
        void register_duplicatePhone_throwsConflict() {
            RegisterRequest req = phoneRegisterRequest();
            when(userRepository.existsByPhone(req.getPhone())).thenReturn(true);

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(UserAlreadyExistsException.class)
                    .hasMessageContaining(req.getPhone());
        }

        @Test
        @DisplayName("stores email in lowercase")
        void register_emailStoredLowercase() {
            RegisterRequest req = emailRegisterRequest();
            req.setEmail("JOHN@EXAMPLE.COM");
            when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
            when(passwordEncoder.encode(any())).thenReturn("hash");
            when(userRepository.save(any())).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                return User.builder().id(1L).email(u.getEmail())
                        .firstName(u.getFirstName()).lastName(u.getLastName())
                        .password(u.getPassword()).role("ROLE_USER").build();
            });
            when(jwtTokenProvider.generateTokenFromUsername(anyString())).thenReturn("tok");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            authService.register(req);

            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getEmail()).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("password is BCrypt-encoded before saving")
        void register_passwordEncoded() {
            RegisterRequest req = emailRegisterRequest();
            when(userRepository.existsByEmail(any())).thenReturn(false);
            when(passwordEncoder.encode("Password123!")).thenReturn("$2a$12$encoded");
            when(userRepository.save(any())).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                return User.builder().id(1L).email(u.getEmail())
                        .firstName(u.getFirstName()).lastName(u.getLastName())
                        .password(u.getPassword()).role("ROLE_USER").build();
            });
            when(jwtTokenProvider.generateTokenFromUsername(any())).thenReturn("tok");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            authService.register(req);

            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getPassword()).isEqualTo("$2a$12$encoded");
        }
    }

    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("returns AuthResponse with token on valid credentials")
        void login_success_byEmail() {
            LoginRequest req = new LoginRequest();
            req.setIdentifier("john@example.com");
            req.setPassword("Password123!");

            Authentication auth = mock(Authentication.class);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(auth);
            when(jwtTokenProvider.generateToken(auth)).thenReturn("valid-jwt");
            when(userRepository.findByEmail("john@example.com"))
                    .thenReturn(Optional.of(savedUser()));

            AuthResponse response = authService.login(req);

            assertThat(response.getToken()).isEqualTo("valid-jwt");
            assertThat(response.getEmail()).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("resolves user by phone when identifier has no '@'")
        void login_success_byPhone() {
            LoginRequest req = new LoginRequest();
            req.setIdentifier("+923001234567");
            req.setPassword("Password123!");

            Authentication auth = mock(Authentication.class);
            when(authenticationManager.authenticate(any())).thenReturn(auth);
            when(jwtTokenProvider.generateToken(auth)).thenReturn("phone-jwt");

            User phoneUser = User.builder()
                    .id(2L).firstName("Jane").lastName("Smith")
                    .phone("+923001234567").password("hash").role("ROLE_USER").build();
            when(userRepository.findByPhone("+923001234567")).thenReturn(Optional.of(phoneUser));

            AuthResponse response = authService.login(req);

            assertThat(response.getToken()).isEqualTo("phone-jwt");
            assertThat(response.getPhone()).isEqualTo("+923001234567");
        }

        @Test
        @DisplayName("throws InvalidCredentialsException on bad password")
        void login_wrongPassword_throws() {
            LoginRequest req = new LoginRequest();
            req.setIdentifier("john@example.com");
            req.setPassword("WrongPassword");

            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("bad creds"));

            assertThatThrownBy(() -> authService.login(req))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessageContaining("Invalid credentials");
        }
    }

    @Nested
    @DisplayName("changePassword()")
    class ChangePassword {

        @Test
        @DisplayName("changes password successfully")
        void changePassword_success() {
            ChangePasswordRequest req = new ChangePasswordRequest();
            req.setCurrentPassword("OldPass123!");
            req.setNewPassword("NewPass456!");
            req.setConfirmNewPassword("NewPass456!");

            User user = savedUser();
            when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("OldPass123!", user.getPassword())).thenReturn(true);
            when(passwordEncoder.encode("NewPass456!")).thenReturn("$2a$12$newHash");

            authService.changePassword("john@example.com", req);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getPassword()).isEqualTo("$2a$12$newHash");
        }

        @Test
        @DisplayName("throws BadRequestException when newPassword != confirmNewPassword")
        void changePassword_mismatch_throws() {
            ChangePasswordRequest req = new ChangePasswordRequest();
            req.setCurrentPassword("OldPass123!");
            req.setNewPassword("NewPass456!");
            req.setConfirmNewPassword("DifferentPass!");

            assertThatThrownBy(() -> authService.changePassword("john@example.com", req))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("do not match");
        }

        @Test
        @DisplayName("throws BadRequestException when new password same as current")
        void changePassword_sameAsOld_throws() {
            ChangePasswordRequest req = new ChangePasswordRequest();
            req.setCurrentPassword("SamePass123!");
            req.setNewPassword("SamePass123!");
            req.setConfirmNewPassword("SamePass123!");

            assertThatThrownBy(() -> authService.changePassword("john@example.com", req))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("different from the current");
        }

        @Test
        @DisplayName("throws InvalidCredentialsException when current password is wrong")
        void changePassword_wrongCurrentPassword_throws() {
            ChangePasswordRequest req = new ChangePasswordRequest();
            req.setCurrentPassword("WrongOld!");
            req.setNewPassword("NewPass456!");
            req.setConfirmNewPassword("NewPass456!");

            User user = savedUser();
            when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("WrongOld!", user.getPassword())).thenReturn(false);

            assertThatThrownBy(() -> authService.changePassword("john@example.com", req))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessageContaining("Current password is incorrect");
        }
    }
}
