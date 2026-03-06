package com.vidara.tradecenter.admin.service;

import java.util.Map;

public interface DashboardService {

    // ==================== COUNT QUERIES ====================

    long getTotalProducts();

    long getTotalUsers();

    long getTotalCategories();

    // ==================== AGGREGATED STATS ====================

    Map<String, Object> getDashboardStats();
}
