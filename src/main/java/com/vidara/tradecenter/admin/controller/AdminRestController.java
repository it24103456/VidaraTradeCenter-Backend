package com.vidara.tradecenter.admin.controller;

import com.vidara.tradecenter.admin.service.DashboardService;
import com.vidara.tradecenter.common.dto.ApiResponse;
import com.vidara.tradecenter.user.model.enums.UserStatus;
import com.vidara.tradecenter.user.repository.UserRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRestController {

    private final DashboardService dashboardService;
    private final UserRepository userRepository;

    public AdminRestController(DashboardService dashboardService, UserRepository userRepository) {
        this.dashboardService = dashboardService;
        this.userRepository = userRepository;
    }


    // ==================== DASHBOARD STATS ====================

    @GetMapping("/dashboard/stats")
    public ApiResponse<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = dashboardService.getDashboardStats();

        // Add user status counts
        stats.put("activeUsers", userRepository.countByStatus(UserStatus.ACTIVE));
        stats.put("inactiveUsers", userRepository.countByStatus(UserStatus.INACTIVE));
        stats.put("bannedUsers", userRepository.countByStatus(UserStatus.BANNED));

        return ApiResponse.success("Dashboard stats retrieved successfully", stats);
    }
}
