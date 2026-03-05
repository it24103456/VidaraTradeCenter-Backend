package com.vidara.tradecenter.admin.controller;

import com.vidara.tradecenter.user.model.User;
import com.vidara.tradecenter.user.model.enums.UserRole;
import com.vidara.tradecenter.user.model.enums.UserStatus;
import com.vidara.tradecenter.user.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserRepository userRepository;

    public AdminUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ==================== LIST ALL USERS ====================

    @GetMapping
    public String listUsers(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            Model model) {

        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> usersPage;

        // Apply filters
        if (status != null && !status.isEmpty()) {
            usersPage = userRepository.findByStatus(UserStatus.valueOf(status), pageable);
        } else if (search != null && !search.isEmpty()) {
            usersPage = userRepository.searchUsers(search, pageable);
        } else {
            usersPage = userRepository.findAll(pageable);
        }

        // Stats
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByStatus(UserStatus.ACTIVE);
        long inactiveUsers = userRepository.countByStatus(UserStatus.INACTIVE);
        long bannedUsers = userRepository.countByStatus(UserStatus.BANNED);

        // Add attributes to model
        model.addAttribute("users", usersPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usersPage.getTotalPages());
        model.addAttribute("totalElements", usersPage.getTotalElements());
        model.addAttribute("pageSize", size);

        // Stats
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("activeUsers", activeUsers);
        model.addAttribute("inactiveUsers", inactiveUsers);
        model.addAttribute("bannedUsers", bannedUsers);

        // Filter values (to preserve in form)
        model.addAttribute("search", search);
        model.addAttribute("status", status);
        model.addAttribute("role", role);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("direction", direction);

        // Page title
        model.addAttribute("pageTitle", "User Management");

        return "admin/users/list";
    }


    // ==================== UPDATE USER STATUS (AJAX) ====================

    @PostMapping("/{id}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateUserStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        Map<String, Object> response = new HashMap<>();

        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            UserStatus newStatus = UserStatus.valueOf(status);
            user.setStatus(newStatus);
            userRepository.save(user);

            response.put("success", true);
            response.put("message", "User status updated to " + newStatus);
            response.put("newStatus", newStatus.name());
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", "Invalid status value");
            return ResponseEntity.badRequest().body(response);

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }


    // ==================== VIEW USER DETAILS ====================

    @GetMapping("/{id}")
    public String viewUser(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {

        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found");
            return "redirect:/admin/users";
        }

        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "User Details");

        return "redirect:/admin/users";
    }
}
