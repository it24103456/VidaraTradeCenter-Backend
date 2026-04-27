package com.vidara.tradecenter.auth.service;

import com.vidara.tradecenter.auth.dto.request.LoginRequest;
import com.vidara.tradecenter.auth.dto.request.RegisterRequest;
import com.vidara.tradecenter.auth.dto.request.ResetPasswordRequest;
import com.vidara.tradecenter.auth.dto.response.AuthResponse;
import com.vidara.tradecenter.auth.dto.response.UserResponse;

public interface AuthService {

    // Register a new user
    AuthResponse register(RegisterRequest request);

    // Login an existing user
    AuthResponse login(LoginRequest request);

    // Get current logged-in user details
    UserResponse getCurrentUser(Long userId);

    // Send forgot password email with reset link
    void sendForgotPasswordEmail(String email);

    // Verify reset token and reset password
    void resetPassword(ResetPasswordRequest request);

    // Verify if reset token is valid
    boolean isResetTokenValid(String token);
}
