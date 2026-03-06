package com.vidara.tradecenter.admin.controller;

import com.vidara.tradecenter.common.dto.ApiResponse;
import com.vidara.tradecenter.user.model.User;
import com.vidara.tradecenter.user.model.enums.UserStatus;
import com.vidara.tradecenter.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserRestController {

    private final UserRepository userRepository;

    public AdminUserRestController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    // ==================== LIST USERS (PAGINATED) ====================

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> listUsers(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> usersPage;

        if (status != null && !status.isEmpty()) {
            usersPage = userRepository.findByStatus(UserStatus.valueOf(status), pageable);
        } else if (search != null && !search.isEmpty()) {
            usersPage = userRepository.searchUsers(search, pageable);
        } else {
            usersPage = userRepository.findAll(pageable);
        }

        // Convert to safe response (no password)
        List<Map<String, Object>> users = usersPage.getContent().stream()
                .map(this::toUserMap)
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("users", users);
        result.put("currentPage", page);
        result.put("totalPages", usersPage.getTotalPages());
        result.put("totalElements", usersPage.getTotalElements());
        result.put("pageSize", size);

        // Stats
        result.put("totalUsers", userRepository.count());
        result.put("activeUsers", userRepository.countByStatus(UserStatus.ACTIVE));
        result.put("inactiveUsers", userRepository.countByStatus(UserStatus.INACTIVE));
        result.put("bannedUsers", userRepository.countByStatus(UserStatus.BANNED));

        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", result));
    }


    // ==================== UPDATE USER STATUS ====================

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStatus(UserStatus.valueOf(status));
        userRepository.save(user);

        return ResponseEntity.ok(ApiResponse.success("User status updated to " + status));
    }


    // ==================== DELETE USER ====================

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.delete(user);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }


    // ==================== HELPER ====================

    private Map<String, Object> toUserMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("firstName", user.getFirstName());
        map.put("lastName", user.getLastName());
        map.put("email", user.getEmail());
        map.put("phone", user.getPhone());
        map.put("status", user.getStatus() != null ? user.getStatus().name() : null);
        map.put("createdAt", user.getCreatedAt());
        map.put("roles", user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet()));
        return map;
    }
}
