package com.vidara.tradecenter.order.controller;

import com.vidara.tradecenter.common.dto.ApiResponse;
import com.vidara.tradecenter.common.exception.BadRequestException;
import com.vidara.tradecenter.common.exception.ResourceNotFoundException;
import com.vidara.tradecenter.order.dto.DeliveryStatusResponse;
import com.vidara.tradecenter.order.dto.OrderListResponse;
import com.vidara.tradecenter.order.model.Order;
import com.vidara.tradecenter.order.repository.OrderRepository;
import com.vidara.tradecenter.order.service.DeliveryTrackingService;
import com.vidara.tradecenter.security.CurrentUser;
import com.vidara.tradecenter.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final DeliveryTrackingService deliveryTrackingService;  // ← NEW

    public OrderController(OrderRepository orderRepository,
                           DeliveryTrackingService deliveryTrackingService) {  // ← UPDATED
        this.orderRepository = orderRepository;
        this.deliveryTrackingService = deliveryTrackingService;  // ← NEW
    }

    @GetMapping
    @Transactional
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyOrders(
            @CurrentUser CustomUserDetails currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        Page<Order> orderPage = orderRepository.findByUserIdOrderByOrderDateDesc(
                currentUser.getId(), pageable);

        List<OrderListResponse> orders = orderPage.getContent().stream()
                .map(OrderListResponse::fromEntity)
                .toList();

        Map<String, Object> result = new HashMap<>();
        result.put("orders", orders);
        result.put("currentPage", orderPage.getNumber());
        result.put("totalPages", orderPage.getTotalPages());
        result.put("totalOrders", orderPage.getTotalElements());

        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", result));
    }

    @GetMapping("/{orderNumber}")
    @Transactional
    public ResponseEntity<ApiResponse<OrderListResponse>> getOrder(
            @CurrentUser CustomUserDetails currentUser,
            @PathVariable String orderNumber) {

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));

        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new BadRequestException("Order does not belong to this user");
        }

        OrderListResponse response = OrderListResponse.fromEntityDetailed(order);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", response));
    }


    // ===== NEW DELIVERY TRACKING ENDPOINTS =====

    /**
     * Get delivery status for an order by order number
     * GET /api/orders/{orderNumber}/delivery-status
     */
    @GetMapping("/{orderNumber}/delivery-status")
    public ResponseEntity<ApiResponse<DeliveryStatusResponse>> getDeliveryStatus(
            @CurrentUser CustomUserDetails currentUser,
            @PathVariable String orderNumber) {

        DeliveryStatusResponse response = deliveryTrackingService.getDeliveryStatus(
                currentUser.getId(), orderNumber);

        return ResponseEntity.ok(ApiResponse.success(
                "Delivery status retrieved successfully", response));
    }

    /**
     * Get delivery status for an order by order ID
     * GET /api/orders/id/{orderId}/delivery-status
     */
    @GetMapping("/id/{orderId}/delivery-status")
    public ResponseEntity<ApiResponse<DeliveryStatusResponse>> getDeliveryStatusById(
            @CurrentUser CustomUserDetails currentUser,
            @PathVariable Long orderId) {

        DeliveryStatusResponse response = deliveryTrackingService.getDeliveryStatusByOrderId(
                currentUser.getId(), orderId);

        return ResponseEntity.ok(ApiResponse.success(
                "Delivery status retrieved successfully", response));
    }

    /**
     * Get all deliveries for current user
     * GET /api/orders/my-deliveries
     */
    @GetMapping("/my-deliveries")
    public ResponseEntity<ApiResponse<List<DeliveryStatusResponse>>> getMyDeliveries(
            @CurrentUser CustomUserDetails currentUser) {

        List<DeliveryStatusResponse> deliveries = deliveryTrackingService.getUserDeliveries(
                currentUser.getId());

        return ResponseEntity.ok(ApiResponse.success(
                "Deliveries retrieved successfully", deliveries));
    }
}