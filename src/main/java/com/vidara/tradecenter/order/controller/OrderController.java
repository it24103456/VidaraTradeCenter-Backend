package com.vidara.tradecenter.order.controller;

import com.vidara.tradecenter.common.dto.ApiResponse;
import com.vidara.tradecenter.common.dto.PagedResponse;
import com.vidara.tradecenter.order.dto.OrderHistoryFilterRequest;
import com.vidara.tradecenter.order.dto.OrderHistoryResponse;
import com.vidara.tradecenter.order.dto.OrderListResponse;
import com.vidara.tradecenter.order.dto.OrderResponse;
import com.vidara.tradecenter.order.dto.OrderStatusResponse;
import com.vidara.tradecenter.order.service.OrderService;
import com.vidara.tradecenter.security.CurrentUser;
import com.vidara.tradecenter.security.CustomUserDetails;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<PagedResponse<OrderHistoryResponse>>> getOrderHistory(
            @CurrentUser CustomUserDetails currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        OrderHistoryFilterRequest filterRequest = new OrderHistoryFilterRequest(
                startDate, endDate, status, search);
        PagedResponse<OrderHistoryResponse> response = orderService.getOrderHistory(
                currentUser.getId(), filterRequest, page, size);

        return ResponseEntity.ok(ApiResponse.success("Order history retrieved successfully", response));
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetails(
            @CurrentUser CustomUserDetails currentUser,
            @PathVariable("id") Long id) {

        OrderResponse response = orderService.getOrderDetails(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Order details retrieved successfully", response));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderStatusResponse>> getOrderStatus(
            @CurrentUser CustomUserDetails currentUser,
            @PathVariable("id") Long id) {

        OrderStatusResponse response = orderService.getOrderStatus(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Order status retrieved successfully", response));
    }

    // Backward-compatible alias for existing clients.
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyOrders(
            @CurrentUser CustomUserDetails currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PagedResponse<OrderHistoryResponse> response = orderService.getOrderHistory(
                currentUser.getId(),
                new OrderHistoryFilterRequest(),
                page,
                size);

        Map<String, Object> result = new HashMap<>();
        result.put("orders", response.getContent());
        result.put("currentPage", response.getPage());
        result.put("totalPages", response.getTotalPages());
        result.put("totalOrders", response.getTotalElements());

        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", result));
    }

    // Legacy endpoint for frontend compatibility.
    @GetMapping("/{orderNumber}")
    public ResponseEntity<ApiResponse<OrderListResponse>> getOrderByOrderNumber(
            @CurrentUser CustomUserDetails currentUser,
            @PathVariable String orderNumber) {

        OrderListResponse response = orderService.getOrderDetailsByOrderNumber(currentUser.getId(), orderNumber);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", response));
    }
}
