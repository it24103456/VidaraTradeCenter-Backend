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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final DeliveryTrackingService deliveryTrackingService;

    public OrderController(OrderRepository orderRepository,
                           DeliveryTrackingService deliveryTrackingService) {
        this.orderRepository = orderRepository;
        this.deliveryTrackingService = deliveryTrackingService;
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

        boolean owner = order.getUser().getId().equals(currentUser.getId());
        boolean admin = currentUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
        if (!owner && !admin) {
            throw new BadRequestException("Order does not belong to this user");
        }

        OrderListResponse response = OrderListResponse.fromEntityDetailed(order);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", response));
    }

    /**
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