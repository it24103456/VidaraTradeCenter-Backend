package com.vidara.tradecenter.auth.service.impl;

import com.vidara.tradecenter.auth.dto.request.LoginRequest;
import com.vidara.tradecenter.auth.dto.request.RegisterRequest;
import com.vidara.tradecenter.auth.dto.request.ResetPasswordRequest;
import com.vidara.tradecenter.auth.dto.response.AuthResponse;
import com.vidara.tradecenter.auth.dto.response.UserResponse;
import com.vidara.tradecenter.auth.mapper.AuthMapper;
import com.vidara.tradecenter.auth.service.AuthService;
import com.vidara.tradecenter.common.exception.DuplicateResourceException;
import com.vidara.tradecenter.common.exception.ResourceNotFoundException;
import com.vidara.tradecenter.common.exception.UnauthorizedException;
import com.vidara.tradecenter.notification.dto.PasswordResetEmail;
import com.vidara.tradecenter.notification.service.EmailNotificationService;
import com.vidara.tradecenter.security.jwt.JwtTokenProvider;
import com.vidara.tradecenter.user.model.Role;
import com.vidara.tradecenter.user.model.User;
import com.vidara.tradecenter.user.model.enums.UserRole;
import com.vidara.tradecenter.user.repository.RoleRepository;
import com.vidara.tradecenter.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

        private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
        private static final long PASSWORD_RESET_TOKEN_EXPIRY_MS = 3600000; // 1 hour

        private final String frontendUrl;
        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtTokenProvider jwtTokenProvider;
        private final AuthenticationManager authenticationManager;
        private final AuthMapper authMapper;
        private final EmailNotificationService emailNotificationService;

        public AuthServiceImpl(UserRepository userRepository,
                        RoleRepository roleRepository,
                        PasswordEncoder passwordEncoder,
                        JwtTokenProvider jwtTokenProvider,
                        AuthenticationManager authenticationManager,
                        AuthMapper authMapper,
                        EmailNotificationService emailNotificationService,
                        @Value("${payhere.frontend-url:http://localhost:3000}") String frontendUrl) {
                this.userRepository = userRepository;
                this.roleRepository = roleRepository;
                this.passwordEncoder = passwordEncoder;
                this.jwtTokenProvider = jwtTokenProvider;
                this.authenticationManager = authenticationManager;
                this.authMapper = authMapper;
                this.emailNotificationService = emailNotificationService;
                this.frontendUrl = frontendUrl;
        }

        // REGISTER

        @Override
        @Transactional
        public AuthResponse register(RegisterRequest request) {
                // Check if email already exists
                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new DuplicateResourceException("User", "email", request.getEmail());
                }

                // Map RegisterRequest → User entity
                User user = authMapper.toUser(request);

                // Encode password
                user.setPassword(passwordEncoder.encode(request.getPassword()));

                // Assign default role (CUSTOMER)
                Role customerRole = roleRepository.findByName(UserRole.CUSTOMER)
                                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", UserRole.CUSTOMER));
                user.addRole(customerRole);

                // Save user
                User savedUser = userRepository.save(user);
                logger.info("User registered successfully: {}", savedUser.getEmail());

                // Generate JWT token
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
                String token = jwtTokenProvider.generateToken(authentication);

                // Map to AuthResponse
                return authMapper.toAuthResponse(savedUser, token);
        }

        // LOGIN

        @Override
        public AuthResponse login(LoginRequest request) {
                // Authenticate with Spring Security
                Authentication authentication;
                try {
                        authentication = authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(request.getEmail(),
                                                        request.getPassword()));
                } catch (BadCredentialsException ex) {
                        throw new UnauthorizedException("Invalid email or password");
                }

                // Generate JWT token
                String token = jwtTokenProvider.generateToken(authentication);

                // Get user from database
                User user = userRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

                logger.info("User logged in successfully: {}", user.getEmail());

                // Map to AuthResponse
                return authMapper.toAuthResponse(user, token);
        }

        // GET CURRENT USER

        @Override
        public UserResponse getCurrentUser(Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

                // Map User → UserResponse
                UserResponse response = new UserResponse();
                response.setId(user.getId());
                response.setFirstName(user.getFirstName());
                response.setLastName(user.getLastName());
                response.setEmail(user.getEmail());
                response.setPhone(user.getPhone());
                response.setProfilePicture(user.getProfilePicture());
                response.setStatus(user.getStatus().name());
                response.setRoles(
                                user.getRoles().stream()
                                                .map(role -> role.getName().name())
                                                .collect(Collectors.toSet()));
                response.setCreatedAt(user.getCreatedAt());

                return response;
        }

        // FORGOT PASSWORD

        @Override
        @Transactional(readOnly = false)
        public void sendForgotPasswordEmail(String email) {
                User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

                // Generate reset token
                String resetToken = UUID.randomUUID().toString();
                long expiryTime = System.currentTimeMillis() + PASSWORD_RESET_TOKEN_EXPIRY_MS;

                // Save token to user
                user.setPasswordResetToken(resetToken);
                user.setPasswordResetTokenExpiry(expiryTime);
                userRepository.save(user);

                // Build reset link
                String resetLink = frontendUrl + "/reset-password?token=" + resetToken;

                // Send email
                PasswordResetEmail emailData = new PasswordResetEmail();
                emailData.setCustomerEmail(user.getEmail());
                emailData.setCustomerName(user.getFirstName());
                emailData.setResetLink(resetLink);

                try {
                        emailNotificationService.sendPasswordResetEmail(emailData);
                        logger.info("Password reset email sent to: {}", email);
                } catch (Exception e) {
                        logger.error("Failed to send password reset email to: {}", email, e);
                        throw new RuntimeException("Failed to send password reset email", e);
                }
        }

        // RESET PASSWORD

        @Override
        @Transactional(readOnly = false)
        public void resetPassword(ResetPasswordRequest request) {
                // Validate passwords match
                if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                        throw new UnauthorizedException("Passwords do not match");
                }

                // Find user by token
                User user = userRepository.findByPasswordResetToken(request.getToken())
                        .orElseThrow(() -> new ResourceNotFoundException("Password reset token", "token", request.getToken()));

                // Check if token is expired
                if (user.getPasswordResetTokenExpiry() == null || System.currentTimeMillis() > user.getPasswordResetTokenExpiry()) {
                        throw new UnauthorizedException("Password reset token has expired");
                }

                // Update password
                user.setPassword(passwordEncoder.encode(request.getNewPassword()));
                user.setPasswordResetToken(null);
                user.setPasswordResetTokenExpiry(null);
                userRepository.save(user);

                logger.info("Password reset successfully for user: {}", user.getEmail());
        }

        // VERIFY RESET TOKEN

        @Override
        public boolean isResetTokenValid(String token) {
                return userRepository.findByPasswordResetToken(token)
                        .map(user -> user.getPasswordResetTokenExpiry() != null && System.currentTimeMillis() <= user.getPasswordResetTokenExpiry())
                        .orElse(false);
        }
}
