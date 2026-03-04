package com.vidara.tradecenter.auth.service;

import com.vidara.tradecenter.auth.dto.request.LoginRequest;
import com.vidara.tradecenter.auth.dto.request.RegisterRequest;
import com.vidara.tradecenter.auth.dto.response.AuthResponse;
import com.vidara.tradecenter.auth.dto.response.UserResponse;

public interface AuthService {

    // Register a new user
    AuthResponse register(RegisterRequest request);

    // Login an existing user
    AuthResponse login(LoginRequest request);

    // Get current logged-in user details
    UserResponse getCurrentUser(Long userId);
}
