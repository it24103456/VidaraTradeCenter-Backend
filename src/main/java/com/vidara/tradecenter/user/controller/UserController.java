package com.vidara.tradecenter.user.controller;

import com.vidara.tradecenter.common.dto.ApiResponse;
import com.vidara.tradecenter.security.CurrentUser;
import com.vidara.tradecenter.security.CustomUserDetails;
import com.vidara.tradecenter.user.dto.request.UpdateProfileRequest;
import com.vidara.tradecenter.user.dto.response.UserResponse;
import com.vidara.tradecenter.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    // GET PROFILE
    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(@CurrentUser CustomUserDetails currentUser) {
        UserResponse userResponse = userService.getProfile(currentUser.getId());
        return ResponseEntity
                .ok(ApiResponse.success("Profile retrieved successfully", userResponse));
    }


    // UPDATE PROFILE
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @CurrentUser CustomUserDetails currentUser,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse userResponse = userService.updateProfile(currentUser.getId(), request);
        return ResponseEntity
                .ok(ApiResponse.success("Profile updated successfully", userResponse));
    }
}
