package com.contactmanagement.service;

import com.contactmanagement.dto.AuthResponse;
import com.contactmanagement.dto.ChangePasswordRequest;
import com.contactmanagement.dto.LoginRequest;
import com.contactmanagement.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void changePassword(String username, ChangePasswordRequest changePasswordRequest);
}
