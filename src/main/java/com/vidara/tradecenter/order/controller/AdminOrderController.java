package com.vidara.tradecenter.order.controller;

import com.vidara.tradecenter.common.dto.ApiResponse;
import com.vidara.tradecenter.order.dto.OrderListResponse;
import com.vidara.tradecenter.order.dto.OrderStatisticsResponse;
import com.vidara.tradecenter.order.dto.UpdateOrderStatusRequest;
import com.vidara.tradecenter.order.service.AdminOrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private static final Logger logger = LoggerFactory.getLogger(AdminOrderController.class);

    private final AdminOrderService adminOrderService;

    public AdminOrderController(AdminOrderService adminOrderService) {
        this.adminOrderService = adminOrderService;
    }


    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(required = false) String search,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        if (size > 100) {
            size = 100;
        }

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<OrderListResponse> orderPage = adminOrderService.getAllOrders(
                status, paymentStatus, search, startDate, endDate, pageable);

        Map<String, Object> data = new HashMap<>();
        data.put("orders", orderPage.getContent());
        data.put("currentPage", orderPage.getNumber());
        data.put("totalItems", orderPage.getTotalElements());
        data.put("totalPages", orderPage.getTotalPages());
        data.put("pageSize", orderPage.getSize());
        data.put("hasNext", orderPage.hasNext());
        data.put("hasPrevious", orderPage.hasPrevious());

        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", data));
    }


    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<OrderStatisticsResponse>> getOrderStatistics() {
        logger.info("Admin requesting order statistics");
        OrderStatisticsResponse stats = adminOrderService.getStatistics();
        return ResponseEntity.ok(ApiResponse.success("Statistics retrieved successfully", stats));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderListResponse>> getOrderById(@PathVariable Long id) {
        logger.info("Admin requesting order details for ID: {}", id);
        OrderListResponse order = adminOrderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", order));
    }


    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderListResponse>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        logger.info("Admin updating order {} status to {}", id, request.getNewStatus());
        OrderListResponse updatedOrder = adminOrderService.updateOrderStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("Order status updated successfully", updatedOrder));
    }
}