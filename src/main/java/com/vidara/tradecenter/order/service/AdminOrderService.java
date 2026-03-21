package com.vidara.tradecenter.order.service;

import com.vidara.tradecenter.common.exception.BadRequestException;
import com.vidara.tradecenter.common.exception.ResourceNotFoundException;
import com.vidara.tradecenter.order.dto.OrderListResponse;
import com.vidara.tradecenter.order.dto.OrderStatisticsResponse;
import com.vidara.tradecenter.order.dto.UpdateOrderStatusRequest;
import com.vidara.tradecenter.order.model.Order;
import com.vidara.tradecenter.order.model.enums.OrderStatus;
import com.vidara.tradecenter.order.model.enums.PaymentStatus;
import com.vidara.tradecenter.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

@Service
public class AdminOrderService {

    private static final Logger logger = LoggerFactory.getLogger(AdminOrderService.class);

    private final OrderRepository orderRepository;

    public AdminOrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }


    @Transactional(readOnly = true)
    public Page<OrderListResponse> getAllOrders(
            String status,
            String paymentStatus,
            String search,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {

        logger.info("Admin fetching orders - status: {}, paymentStatus: {}, search: {}, page: {}",
                status, paymentStatus, search, pageable.getPageNumber());

        // Validate status values if provided
        String statusStr = null;
        if (status != null && !status.trim().isEmpty()) {
            statusStr = parseOrderStatus(status).name();
        }

        String paymentStatusStr = null;
        if (paymentStatus != null && !paymentStatus.trim().isEmpty()) {
            paymentStatusStr = parsePaymentStatus(paymentStatus).name();
        }

        String searchQuery = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        Page<Order> orders = orderRepository.findOrdersWithFilters(
                statusStr, paymentStatusStr, searchQuery, startDate, endDate, pageable);

        return orders.map(OrderListResponse::fromEntity);
    }


    @Transactional(readOnly = true)
    public OrderListResponse getOrderById(Long orderId) {
        logger.info("Admin fetching order details for ID: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        return OrderListResponse.fromEntityDetailed(order);
    }


    @Transactional
    public OrderListResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        logger.info("Admin updating order {} status to {}", orderId, request.getNewStatus());

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(request.getNewStatus().toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(
                    "Invalid status: '" + request.getNewStatus() + "'. Valid values: " +
                            Arrays.toString(OrderStatus.values()));
        }

        OrderStatus currentStatus = order.getOrderStatus();
        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new BadRequestException(
                    "Invalid status transition: " + currentStatus + " → " + newStatus);
        }

        order.setOrderStatus(newStatus);

        if (newStatus == OrderStatus.PAID) {
            order.setPaymentStatus(PaymentStatus.COMPLETED);
        }

        Order savedOrder = orderRepository.save(order);
        logger.info("Order {} status updated: {} → {}", orderId, currentStatus, newStatus);

        return OrderListResponse.fromEntity(savedOrder);
    }


    @Transactional(readOnly = true)
    public OrderStatisticsResponse getStatistics() {
        logger.info("Admin fetching order statistics");

        long totalOrders = orderRepository.count();
        BigDecimal totalRevenue = orderRepository.sumTotalAmount();

        long pending = orderRepository.countByOrderStatus(OrderStatus.PENDING);
        long paid = orderRepository.countByOrderStatus(OrderStatus.PAID);
        long processing = orderRepository.countByOrderStatus(OrderStatus.PROCESSING);
        long shipped = orderRepository.countByOrderStatus(OrderStatus.SHIPPED);
        long delivered = orderRepository.countByOrderStatus(OrderStatus.DELIVERED);
        long cancelled = orderRepository.countByOrderStatus(OrderStatus.CANCELLED);

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        long todayOrders = orderRepository.countByOrderDateAfter(todayStart);
        BigDecimal todayRevenue = orderRepository.sumTotalAmountAfter(todayStart);

        return new OrderStatisticsResponse(
                totalOrders,
                totalRevenue != null ? totalRevenue : BigDecimal.ZERO,
                pending,
                paid,
                processing,
                shipped,
                delivered,
                cancelled,
                todayOrders,
                todayRevenue != null ? todayRevenue : BigDecimal.ZERO
        );
    }


    private OrderStatus parseOrderStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return null;
        }
        try {
            return OrderStatus.valueOf(status.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid order status filter: " + status);
        }
    }

    private PaymentStatus parsePaymentStatus(String paymentStatus) {
        if (paymentStatus == null || paymentStatus.trim().isEmpty()) {
            return null;
        }
        try {
            return PaymentStatus.valueOf(paymentStatus.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid payment status filter: " + paymentStatus);
        }
    }
}