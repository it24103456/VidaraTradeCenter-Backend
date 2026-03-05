package com.vidara.tradecenter.admin.controller;

import com.vidara.tradecenter.admin.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // ==================== REDIRECT ROOT TO DASHBOARD ====================

    @GetMapping("/")
    public String redirectToDashboard() {
        return "redirect:/admin/dashboard";
    }

    // ==================== DASHBOARD HOME ====================

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        // Get all stats from service
        Map<String, Object> stats = dashboardService.getDashboardStats();

        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("totalProducts", stats.get("totalProducts"));
        model.addAttribute("totalCategories", stats.get("totalCategories"));
        model.addAttribute("totalUsers", stats.get("totalUsers"));
        model.addAttribute("totalOrders", stats.get("totalOrders"));
        model.addAttribute("totalRevenue", stats.get("totalRevenue"));
        model.addAttribute("recentProducts", null);

        return "admin/dashboard";
    }
}