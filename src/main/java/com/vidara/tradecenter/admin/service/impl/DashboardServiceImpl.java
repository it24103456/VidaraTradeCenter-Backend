package com.vidara.tradecenter.admin.service.impl;
import com.vidara.tradecenter.admin.service.DashboardService;
import com.vidara.tradecenter.product.repository.CategoryRepository;
import com.vidara.tradecenter.product.repository.ProductRepository;
import com.vidara.tradecenter.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
@Service
public class DashboardServiceImpl implements DashboardService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    @Autowired
    public DashboardServiceImpl(UserRepository userRepository,
                                ProductRepository productRepository,
                                CategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
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
        stats.put("totalProducts", getTotalProducts());
        stats.put("totalUsers", getTotalUsers());
        stats.put("totalCategories", getTotalCategories());
        stats.put("totalOrders", 0L);
        stats.put("totalRevenue", 0.0);
        return stats;
    }
}
