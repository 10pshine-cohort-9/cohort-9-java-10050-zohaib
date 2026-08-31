package com.contactmanagement.exception;

import com.contactmanagement.security.JwtAuthenticationFilter;
import com.contactmanagement.security.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests GlobalExceptionHandler in isolation using a minimal fake controller
 * that deliberately throws each exception type.
 */
@WebMvcTest(
    controllers = GlobalExceptionHandlerTest.FakeController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class
    )
)
@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    JwtTokenProvider jwtTokenProvider;

    @MockBean
    UserDetailsService userDetailsService;

    @RestController
    @RequestMapping("/test")
    static class FakeController {

        @GetMapping("/conflict")
        public void conflict() {
            throw new UserAlreadyExistsException("Email already registered.");
        }

        @GetMapping("/unauthorized")
        public void unauthorized() {
            throw new InvalidCredentialsException("Bad credentials.");
        }

        @GetMapping("/not-found")
        public void notFound() {
            throw new ResourceNotFoundException("Contact", "id", 99L);
        }

        @GetMapping("/bad-request")
        public void badRequest() {
            throw new BadRequestException("Request is invalid.");
        }

        @GetMapping("/access-denied")
        public void accessDenied() {
            throw new AccessDeniedException("Forbidden.");
        }

        @GetMapping("/disabled")
        public void disabled() {
            throw new DisabledException("Account disabled.");
        }

        @GetMapping("/server-error")
        public void serverError() {
            throw new RuntimeException("Something exploded.");
        }

        @Data
        static class ValidatedBody {
            @NotBlank(message = "name is required")
            private String name;
        }

        @PostMapping("/validated")
        public void validated(@Valid @RequestBody ValidatedBody body) {
        }
    }

    @Nested
    @DisplayName("409 Conflict — UserAlreadyExistsException")
    class ConflictHandler {

        @Test
        @WithMockUser
        @DisplayName("returns 409 with message from exception")
        void conflict_returns409() throws Exception {
            mockMvc.perform(get("/test/conflict"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.message").value("Email already registered."))
                    .andExpect(jsonPath("$.timestamp").exists());
        }
    }

    @Nested
    @DisplayName("401 Unauthorized — InvalidCredentialsException")
    class UnauthorizedHandler {

        @Test
        @WithMockUser
        @DisplayName("returns 401 with message from exception")
        void unauthorized_returns401() throws Exception {
            mockMvc.perform(get("/test/unauthorized"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.message").value("Bad credentials."));
        }
    }

    @Nested
    @DisplayName("404 Not Found — ResourceNotFoundException")
    class NotFoundHandler {

        @Test
        @WithMockUser
        @DisplayName("returns 404 with formatted resource message")
        void notFound_returns404() throws Exception {
            mockMvc.perform(get("/test/not-found"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Contact not found with id: '99'"));
        }
    }

    @Nested
    @DisplayName("400 Bad Request — BadRequestException")
    class BadRequestHandler {

        @Test
        @WithMockUser
        @DisplayName("returns 400 with message from exception")
        void badRequest_returns400() throws Exception {
            mockMvc.perform(get("/test/bad-request"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("Request is invalid."));
        }
    }

    @Nested
    @DisplayName("403 Forbidden — AccessDeniedException")
    class AccessDeniedHandler {

        @Test
        @WithMockUser
        @DisplayName("returns 403 with generic 'Access denied' message")
        void accessDenied_returns403() throws Exception {
            mockMvc.perform(get("/test/access-denied"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value(403))
                    .andExpect(jsonPath("$.message").value("Access denied"));
        }
    }

    @Nested
    @DisplayName("401 Unauthorized — DisabledException")
    class DisabledHandler {

        @Test
        @WithMockUser
        @DisplayName("returns 401 with 'Account is disabled' message")
        void disabled_returns401() throws Exception {
            mockMvc.perform(get("/test/disabled"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.message").value("Account is disabled"));
        }
    }

    @Nested
    @DisplayName("500 Internal Server Error — unhandled Exception")
    class ServerErrorHandler {

        @Test
        @WithMockUser
        @DisplayName("returns 500 with generic message for unexpected exceptions")
        void serverError_returns500() throws Exception {
            mockMvc.perform(get("/test/server-error"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.message")
                            .value("An unexpected error occurred. Please try again later."));
        }
    }

    @Nested
    @DisplayName("400 Bad Request — MethodArgumentNotValidException")
    class ValidationErrorHandler {

        @Test
        @WithMockUser
        @DisplayName("returns 400 with per-field errors map when @Valid fails")
        void validation_returns400WithFieldErrors() throws Exception {
            mockMvc.perform(post("/test/validated")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"\"}")
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("Validation failed"))
                    .andExpect(jsonPath("$.errors.name").value("name is required"));
        }

        @Test
        @WithMockUser
        @DisplayName("returns 400 with errors map when required field is null")
        void validation_nullField_returns400() throws Exception {
            mockMvc.perform(post("/test/validated")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}")
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.name").exists());
        }
    }
}
