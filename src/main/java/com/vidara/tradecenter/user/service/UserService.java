package com.vidara.tradecenter.user.service;

import com.vidara.tradecenter.user.dto.request.UpdateProfileRequest;
import com.vidara.tradecenter.user.dto.response.UserResponse;
import com.vidara.tradecenter.user.model.enums.UserStatus;

import java.util.List;

public interface UserService {

    // Get user profile by ID
    UserResponse getProfile(Long userId);

    // Update user profile
    UserResponse updateProfile(Long userId, UpdateProfileRequest request);

    // Get all users (admin)
    List<UserResponse> getAllUsers();

    // Update user status (admin)
    void updateUserStatus(Long userId, UserStatus status);
}
