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

import com.vidara.tradecenter.common.exception.BadRequestException;
import com.vidara.tradecenter.order.dto.DeliveryStatusResponse;
import com.vidara.tradecenter.order.dto.RefundRequest;
import com.vidara.tradecenter.order.dto.RefundResponse;
import com.vidara.tradecenter.order.model.enums.DeliveryStatus;
import com.vidara.tradecenter.order.service.DeliveryTrackingService;
import com.vidara.tradecenter.security.CurrentUser;
import com.vidara.tradecenter.security.CustomUserDetails;
import java.time.LocalDate;
import java.util.List;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private static final Logger logger = LoggerFactory.getLogger(AdminOrderController.class);

    private final AdminOrderService adminOrderService;
    private final DeliveryTrackingService deliveryTrackingService;  // ← NEW

    // Map Java field names to database column names
    private static final Map<String, String> SORT_FIELD_MAP = Map.of(
            "orderDate", "order_date",
            "totalAmount", "total_amount",
            "orderStatus", "order_status",
            "paymentStatus", "payment_status",
            "orderNumber", "order_number",
            "subtotal", "subtotal",
            "shippingCost", "shipping_cost",
            "createdAt", "created_at",
            "id", "id"
    );

    public AdminOrderController(AdminOrderService adminOrderService,
                                DeliveryTrackingService deliveryTrackingService) {  // ← UPDATED
        this.adminOrderService = adminOrderService;
        this.deliveryTrackingService = deliveryTrackingService;  // ← NEW
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

        // Convert Java field name to database column name
        String dbColumn = SORT_FIELD_MAP.getOrDefault(sortBy, "order_date");

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(dbColumn).ascending()
                : Sort.by(dbColumn).descending();

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

    // ===== NEW REFUND ENDPOINT =====

    /**
     * Process refund for an order
     * POST /api/admin/orders/{id}/refund
     */
    @PostMapping("/{id}/refund")
    public ResponseEntity<ApiResponse<RefundResponse>> processRefund(
            @PathVariable Long id,
            @Valid @RequestBody RefundRequest request,
            @CurrentUser CustomUserDetails currentUser) {

        logger.info("Admin {} processing refund for order {}", currentUser.getId(), id);

        RefundResponse response = adminOrderService.processRefund(id, request, currentUser.getId());

        return ResponseEntity.ok(ApiResponse.success(
                "Refund processed successfully", response));
    }


    // ===== NEW DELIVERY TRACKING ADMIN ENDPOINTS =====

    /**
     * Create delivery tracking for an order
     * POST /api/admin/orders/{id}/delivery-tracking
     */
    @PostMapping("/{id}/delivery-tracking")
    public ResponseEntity<ApiResponse<DeliveryStatusResponse>> createDeliveryTracking(
            @PathVariable Long id,
            @RequestParam(required = false) String trackingNumber,
            @RequestParam(required = false) String courierName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate estimatedDeliveryDate) {

        logger.info("Admin creating delivery tracking for order {}", id);

        DeliveryStatusResponse response = deliveryTrackingService.createDeliveryTracking(
                id, trackingNumber, courierName, estimatedDeliveryDate);

        return ResponseEntity.ok(ApiResponse.success(
                "Delivery tracking created successfully", response));
    }


    /**
     * Update delivery status
     * PUT /api/admin/orders/{id}/delivery-status
     */
    @PutMapping("/{id}/delivery-status")
    public ResponseEntity<ApiResponse<DeliveryStatusResponse>> updateDeliveryStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String notes) {

        logger.info("Admin updating delivery status for order {} to {}", id, status);

        DeliveryStatus newStatus;
        try {
            newStatus = DeliveryStatus.valueOf(status.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid delivery status: " + status +
                    ". Valid values: " + java.util.Arrays.toString(DeliveryStatus.values()));
        }

        DeliveryStatusResponse response = deliveryTrackingService.updateDeliveryStatus(
                id, newStatus, notes);

        return ResponseEntity.ok(ApiResponse.success(
                "Delivery status updated successfully", response));
    }

    /**
     * Update tracking information
     * PUT /api/admin/orders/{id}/tracking-info
     */
    @PutMapping("/{id}/tracking-info")
    public ResponseEntity<ApiResponse<DeliveryStatusResponse>> updateTrackingInfo(
            @PathVariable Long id,
            @RequestParam(required = false) String trackingNumber,
            @RequestParam(required = false) String courierName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate estimatedDeliveryDate) {

        logger.info("Admin updating tracking info for order {}", id);

        DeliveryStatusResponse response = deliveryTrackingService.updateTrackingInfo(
                id, trackingNumber, courierName, estimatedDeliveryDate);

        return ResponseEntity.ok(ApiResponse.success(
                "Tracking information updated successfully", response));
    }

    /**
     * Get delivery tracking for an order
     * GET /api/admin/orders/{id}/delivery-tracking
     */
    @GetMapping("/{id}/delivery-tracking")
    public ResponseEntity<ApiResponse<DeliveryStatusResponse>> getDeliveryTracking(
            @PathVariable Long id) {

        DeliveryStatusResponse response = deliveryTrackingService.getDeliveryStatusByOrderId(
                null, id);  // Admin doesn't need user validation

        return ResponseEntity.ok(ApiResponse.success(
                "Delivery tracking retrieved successfully", response));
    }

    /**
     * Get overdue deliveries
     * GET /api/admin/orders/deliveries/overdue
     */
    @GetMapping("/deliveries/overdue")
    public ResponseEntity<ApiResponse<List<DeliveryStatusResponse>>> getOverdueDeliveries() {
        logger.info("Admin fetching overdue deliveries");

        List<DeliveryStatusResponse> deliveries = deliveryTrackingService.getOverdueDeliveries();

        return ResponseEntity.ok(ApiResponse.success(
                "Overdue deliveries retrieved successfully", deliveries));
    }

    /**
     * Get deliveries by status
     * GET /api/admin/orders/deliveries/status/{status}
     */
    @GetMapping("/deliveries/status/{status}")
    public ResponseEntity<ApiResponse<List<DeliveryStatusResponse>>> getDeliveriesByStatus(
            @PathVariable String status) {

        logger.info("Admin fetching deliveries with status {}", status);

        DeliveryStatus deliveryStatus;
        try {
            deliveryStatus = DeliveryStatus.valueOf(status.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid delivery status: " + status);
        }

        List<DeliveryStatusResponse> deliveries = deliveryTrackingService.getDeliveriesByStatus(
                deliveryStatus);

        return ResponseEntity.ok(ApiResponse.success(
                "Deliveries retrieved successfully", deliveries));
    }
}