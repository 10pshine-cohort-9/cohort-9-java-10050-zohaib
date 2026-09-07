package com.contactmanagement.service.impl;

import com.contactmanagement.dto.AuthResponse;
import com.contactmanagement.dto.ChangePasswordRequest;
import com.contactmanagement.dto.LoginRequest;
import com.contactmanagement.dto.RegisterRequest;
import com.contactmanagement.exception.BadRequestException;
import com.contactmanagement.exception.InvalidCredentialsException;
import com.contactmanagement.exception.ResourceNotFoundException;
import com.contactmanagement.exception.UserAlreadyExistsException;
import com.contactmanagement.model.User;
import com.contactmanagement.repository.UserRepository;
import com.contactmanagement.security.JwtTokenProvider;
import com.contactmanagement.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        boolean hasEmail = StringUtils.hasText(request.getEmail());
        boolean hasPhone = StringUtils.hasText(request.getPhone());

        if (!hasEmail && !hasPhone) {
            throw new BadRequestException(
                    "At least one of email or phone number must be provided for registration.");
        }

        if (hasEmail && userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(
                    "An account with email '" + request.getEmail() + "' already exists.");
        }
        if (hasPhone && userRepository.existsByPhone(request.getPhone())) {
            throw new UserAlreadyExistsException(
                    "An account with phone '" + request.getPhone() + "' already exists.");
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(hasEmail ? request.getEmail().toLowerCase().trim() : null)
                .phone(hasPhone ? request.getPhone().trim() : null)
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        user = userRepository.save(user);
        log.info("Registered new user id={} identifier={}",
                user.getId(), hasEmail ? user.getEmail() : user.getPhone());

        String subject = (user.getEmail() != null) ? user.getEmail() : user.getPhone();
        String token = jwtTokenProvider.generateTokenFromUsername(subject);

        return buildAuthResponse(token, user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getIdentifier().trim();

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(identifier, request.getPassword()));

            String token = jwtTokenProvider.generateToken(authentication);
            User user = loadUserByIdentifier(identifier);

            log.info("User logged in: id={} identifier={}", user.getId(), identifier);
            return buildAuthResponse(token, user);

        } catch (DisabledException ex) {
            throw new InvalidCredentialsException("Account is disabled. Please contact support.");
        } catch (BadCredentialsException | UsernameNotFoundException ex) {
            throw new InvalidCredentialsException("Invalid credentials. Please check your email/phone and password.");
        }
    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new BadRequestException("New password and confirmation password do not match.");
        }

        if (request.getCurrentPassword().equals(request.getNewPassword())) {
            throw new BadRequestException("New password must be different from the current password.");
        }

        User user = loadUserByIdentifier(username);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed successfully for user id={}", user.getId());
    }

    private User loadUserByIdentifier(String identifier) {
        if (identifier.contains("@")) {
            return userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "email", identifier));
        }
        return userRepository.findByPhone(identifier)
                .orElseThrow(() -> new ResourceNotFoundException("User", "phone", identifier));
    }

    private AuthResponse buildAuthResponse(String token, User user) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .build();
    }
}
