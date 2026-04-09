package com.vidara.tradecenter.admin.service.impl;

import com.vidara.tradecenter.admin.service.DashboardService;
import com.vidara.tradecenter.order.repository.OrderRepository;
import com.vidara.tradecenter.product.repository.CategoryRepository;
import com.vidara.tradecenter.product.repository.ProductRepository;
import com.vidara.tradecenter.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;

    @Autowired
    public DashboardServiceImpl(UserRepository userRepository,
                                ProductRepository productRepository,
                                CategoryRepository categoryRepository,
                                OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public long getTotalProducts() {
        return productRepository.count();
    }

    @Override
    public long getTotalUsers() {
        return userRepository.count();
    }

    @Override
    public long getTotalCategories() {
        return categoryRepository.count();
    }

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalOrders = orderRepository.count();

        // Net revenue = gross (completed+refunded) - refunds
        BigDecimal grossRevenue = orderRepository.sumGrossRevenue();
        BigDecimal totalRefunds = orderRepository.sumTotalRefunds();
        BigDecimal totalRevenue = grossRevenue.subtract(totalRefunds);

        // (Optional but useful) today's net revenue
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        BigDecimal todayGrossRevenue = orderRepository.sumGrossRevenueAfter(todayStart);
        BigDecimal todayRefunds = orderRepository.sumRefundsAfter(todayStart);
        BigDecimal todayRevenue = todayGrossRevenue.subtract(todayRefunds);

        stats.put("totalProducts", getTotalProducts());
        stats.put("totalUsers", getTotalUsers());
        stats.put("totalCategories", getTotalCategories());

        stats.put("totalOrders", totalOrders);

        // Keep as number for frontend (toFixed works)
        stats.put("totalRevenue", totalRevenue.doubleValue());
        stats.put("todayRevenue", todayRevenue.doubleValue());

        return stats;
    }
}