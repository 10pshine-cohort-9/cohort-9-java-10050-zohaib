package com.contactmanagement.controller;

import com.contactmanagement.dto.*;
import com.contactmanagement.exception.InvalidCredentialsException;
import com.contactmanagement.exception.UserAlreadyExistsException;
import com.contactmanagement.security.JwtAuthenticationFilter;
import com.contactmanagement.security.JwtTokenProvider;
import com.contactmanagement.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = AuthController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class
    )
)
@DisplayName("AuthController")
class AuthControllerTest {

    @Autowired MockMvc      mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean AuthService        authService;
    @MockBean JwtTokenProvider   jwtTokenProvider;
    @MockBean UserDetailsService userDetailsService;

    private AuthResponse sampleAuthResponse() {
        return AuthResponse.builder()
                .token("test-jwt-token")
                .tokenType("Bearer")
                .userId(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .role("ROLE_USER")
                .build();
    }

    @Nested
    @DisplayName("POST /api/auth/register")
    class Register {

        @Test
        @DisplayName("returns 201 with AuthResponse on valid email registration")
        void register_validEmail_returns201() throws Exception {
            RegisterRequest req = new RegisterRequest();
            req.setFirstName("John");
            req.setLastName("Doe");
            req.setEmail("john@example.com");
            req.setPassword("Password123!");

            when(authService.register(any(RegisterRequest.class))).thenReturn(sampleAuthResponse());

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req))
                            .with(csrf()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.token").value("test-jwt-token"))
                    .andExpect(jsonPath("$.email").value("john@example.com"))
                    .andExpect(jsonPath("$.role").value("ROLE_USER"))
                    .andExpect(jsonPath("$.tokenType").value("Bearer"));
        }

        @Test
        @DisplayName("returns 400 when firstName is blank")
        void register_blankFirstName_returns400() throws Exception {
            RegisterRequest req = new RegisterRequest();
            req.setFirstName("");
            req.setLastName("Doe");
            req.setEmail("john@example.com");
            req.setPassword("Password123!");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req))
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.firstName").exists());
        }

        @Test
        @DisplayName("returns 400 when password is too short")
        void register_shortPassword_returns400() throws Exception {
            RegisterRequest req = new RegisterRequest();
            req.setFirstName("John");
            req.setLastName("Doe");
            req.setEmail("john@example.com");
            req.setPassword("short");

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req))
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.password").exists());
        }

        @Test
        @DisplayName("returns 409 when email is already registered")
        void register_duplicateEmail_returns409() throws Exception {
            RegisterRequest req = new RegisterRequest();
            req.setFirstName("John");
            req.setLastName("Doe");
            req.setEmail("taken@example.com");
            req.setPassword("Password123!");

            when(authService.register(any()))
                    .thenThrow(new UserAlreadyExistsException(
                            "An account with email 'taken@example.com' already exists."));

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req))
                            .with(csrf()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value(
                            "An account with email 'taken@example.com' already exists."));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class Login {

        @Test
        @DisplayName("returns 200 with AuthResponse on valid credentials")
        void login_validCredentials_returns200() throws Exception {
            LoginRequest req = new LoginRequest();
            req.setIdentifier("john@example.com");
            req.setPassword("Password123!");

            when(authService.login(any(LoginRequest.class))).thenReturn(sampleAuthResponse());

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("test-jwt-token"))
                    .andExpect(jsonPath("$.userId").value(1));
        }

        @Test
        @DisplayName("returns 401 on invalid credentials")
        void login_invalidCredentials_returns401() throws Exception {
            LoginRequest req = new LoginRequest();
            req.setIdentifier("john@example.com");
            req.setPassword("WrongPassword");

            when(authService.login(any()))
                    .thenThrow(new InvalidCredentialsException(
                            "Invalid credentials. Please check your email/phone and password."));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req))
                            .with(csrf()))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value(
                            "Invalid credentials. Please check your email/phone and password."));
        }

        @Test
        @DisplayName("returns 400 when identifier is blank")
        void login_blankIdentifier_returns400() throws Exception {
            LoginRequest req = new LoginRequest();
            req.setIdentifier("");
            req.setPassword("Password123!");

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req))
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.identifier").exists());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/change-password")
    class ChangePassword {

        @Test
        @DisplayName("returns 200 on successful password change")
        @WithMockUser(username = "john@example.com", roles = "USER")
        void changePassword_success_returns200() throws Exception {
            ChangePasswordRequest req = new ChangePasswordRequest();
            req.setCurrentPassword("OldPass123!");
            req.setNewPassword("NewPass456!");
            req.setConfirmNewPassword("NewPass456!");

            doNothing().when(authService).changePassword(anyString(), any());

            mockMvc.perform(post("/api/auth/change-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Password changed successfully."));
        }

        @Test
        @DisplayName("returns 401 when wrong current password")
        @WithMockUser(username = "john@example.com", roles = "USER")
        void changePassword_wrongCurrent_returns401() throws Exception {
            ChangePasswordRequest req = new ChangePasswordRequest();
            req.setCurrentPassword("Wrong!");
            req.setNewPassword("NewPass456!");
            req.setConfirmNewPassword("NewPass456!");

            doThrow(new InvalidCredentialsException("Current password is incorrect."))
                    .when(authService).changePassword(anyString(), any());

            mockMvc.perform(post("/api/auth/change-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req))
                            .with(csrf()))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value("Current password is incorrect."));
        }

        @Test
        @DisplayName("returns 401 when request is unauthenticated")
        void changePassword_unauthenticated_returns401() throws Exception {
            ChangePasswordRequest req = new ChangePasswordRequest();
            req.setCurrentPassword("OldPass123!");
            req.setNewPassword("NewPass456!");
            req.setConfirmNewPassword("NewPass456!");

            mockMvc.perform(post("/api/auth/change-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req))
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }
    }
}
