package com.vidara.tradecenter.auth.contoller;

import com.vidara.tradecenter.auth.dto.request.LoginRequest;
import com.vidara.tradecenter.auth.dto.request.RegisterRequest;
import com.vidara.tradecenter.auth.dto.request.ResetPasswordRequest;
import com.vidara.tradecenter.auth.dto.response.AuthResponse;
import com.vidara.tradecenter.auth.dto.response.UserResponse;
import com.vidara.tradecenter.auth.service.AuthService;
import com.vidara.tradecenter.common.dto.ApiResponse;
import com.vidara.tradecenter.security.CurrentUser;
import com.vidara.tradecenter.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // REGISTER
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse authResponse = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", authResponse));
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity
                .ok(ApiResponse.success("Login successful", authResponse));
    }

    // GET CURRENT USER
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@CurrentUser CustomUserDetails currentUser) {
        UserResponse userResponse = authService.getCurrentUser(currentUser.getId());
        return ResponseEntity
                .ok(ApiResponse.success("User details retrieved successfully", userResponse));
    }

    // FORGOT PASSWORD - Send reset email
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@RequestParam String email) {
        authService.sendForgotPasswordEmail(email);
        return ResponseEntity
                .ok(ApiResponse.success("Password reset link has been sent to your email", null));
    }

    // RESET PASSWORD - Reset password with token
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity
                .ok(ApiResponse.success("Password reset successfully", null));
    }

    // VERIFY RESET TOKEN - Check if token is valid
    @GetMapping("/verify-reset-token")
    public ResponseEntity<ApiResponse<Boolean>> verifyResetToken(@RequestParam String token) {
        boolean isValid = authService.isResetTokenValid(token);
        return ResponseEntity
                .ok(ApiResponse.success("Token validation result", isValid));
    }
}
