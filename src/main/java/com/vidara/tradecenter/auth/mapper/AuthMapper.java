package com.vidara.tradecenter.auth.mapper;

import com.vidara.tradecenter.auth.dto.request.RegisterRequest;
import com.vidara.tradecenter.auth.dto.response.AuthResponse;
import com.vidara.tradecenter.user.model.Role;
import com.vidara.tradecenter.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    // Map RegisterRequest DTO → User entity
    public User toUser(RegisterRequest request) {
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        return user;
    }

    // Map User entity + JWT token → AuthResponse DTO
    public AuthResponse toAuthResponse(User user, String token) {
        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());

        // Get the first role name (users typically have one primary role)
        String roleName = user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .map(Enum::name)
                .orElse("CUSTOMER");

        response.setRole(roleName);
        return response;
    }
}
