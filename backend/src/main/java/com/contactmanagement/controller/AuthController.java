package com.contactmanagement.controller;

import com.contactmanagement.dto.ApiResponse;
import com.contactmanagement.dto.AuthResponse;
import com.contactmanagement.dto.ChangePasswordRequest;
import com.contactmanagement.dto.LoginRequest;
import com.contactmanagement.dto.RegisterRequest;
import com.contactmanagement.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        log.debug("Registration request for identifier: {}",
                request.getEmail() != null ? request.getEmail() : request.getPhone());

        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        log.debug("Login attempt for identifier: {}", request.getIdentifier());

        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {

        log.debug("Change-password request for user: {}", userDetails.getUsername());

        authService.changePassword(userDetails.getUsername(), request);

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Password changed successfully.")
                .build());
    }
}
