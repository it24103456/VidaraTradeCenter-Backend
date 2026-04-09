package com.vidara.tradecenter.order.service;

import com.vidara.tradecenter.common.exception.BadRequestException;
import com.vidara.tradecenter.common.exception.ResourceNotFoundException;
import com.vidara.tradecenter.notification.dto.OrderStatusUpdateEmail;
import com.vidara.tradecenter.notification.event.OrderStatusChangedEvent;
import com.vidara.tradecenter.order.dto.OrderListResponse;
import com.vidara.tradecenter.order.dto.OrderStatisticsResponse;
import com.vidara.tradecenter.order.dto.RefundRequest;
import com.vidara.tradecenter.order.dto.RefundResponse;
import com.vidara.tradecenter.order.dto.UpdateOrderStatusRequest;
import com.vidara.tradecenter.order.model.Order;
import com.vidara.tradecenter.order.model.enums.OrderStatus;
import com.vidara.tradecenter.order.model.enums.PaymentStatus;
import com.vidara.tradecenter.order.repository.OrderRepository;
import com.vidara.tradecenter.user.model.User;
import com.vidara.tradecenter.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;

    public AdminOrderService(OrderRepository orderRepository,
                             ApplicationEventPublisher eventPublisher,
                             UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
        this.userRepository = userRepository;
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

        try {
            OrderStatusUpdateEmail emailData = new OrderStatusUpdateEmail(
                    savedOrder.getUser().getFullName(),
                    savedOrder.getUser().getEmail(),
                    savedOrder.getOrderNumber(),
                    currentStatus.name(),
                    newStatus.name());
            eventPublisher.publishEvent(new OrderStatusChangedEvent(this, emailData));
            logger.info("Published OrderStatusChangedEvent for order {} ({} → {})",
                    savedOrder.getOrderNumber(), currentStatus, newStatus);
        } catch (Exception e) {
            logger.warn("Failed to publish order status change event for order {}: {}",
                    orderId, e.getMessage());
        }

        return OrderListResponse.fromEntity(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderStatisticsResponse getStatistics() {
        logger.info("Admin fetching order statistics");

        long totalOrders = orderRepository.count();

        BigDecimal grossRevenue = nz(orderRepository.sumGrossRevenue());
        BigDecimal totalRefunds = nz(orderRepository.sumTotalRefunds());
        BigDecimal totalRevenue = grossRevenue.subtract(totalRefunds);

        long pending = orderRepository.countByOrderStatus(OrderStatus.PENDING);
        long paid = orderRepository.countByOrderStatus(OrderStatus.PAID);
        long processing = orderRepository.countByOrderStatus(OrderStatus.PROCESSING);
        long shipped = orderRepository.countByOrderStatus(OrderStatus.SHIPPED);
        long delivered = orderRepository.countByOrderStatus(OrderStatus.DELIVERED);
        long cancelled = orderRepository.countByOrderStatus(OrderStatus.CANCELLED);

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        long todayOrders = orderRepository.countByOrderDateAfter(todayStart);

        BigDecimal todayGross = nz(orderRepository.sumGrossRevenueAfter(todayStart));
        BigDecimal todayRefunds = nz(orderRepository.sumRefundsAfter(todayStart));
        BigDecimal todayRevenue = todayGross.subtract(todayRefunds);

        return new OrderStatisticsResponse(
                totalOrders,
                totalRevenue,
                pending,
                paid,
                processing,
                shipped,
                delivered,
                cancelled,
                todayOrders,
                todayRevenue
        );
    }

    @Transactional
    public RefundResponse processRefund(Long orderId, RefundRequest request, Long adminUserId) {
        logger.info("Admin {} processing refund for order {}", adminUserId, orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.canRefund()) {
            throw new BadRequestException(
                    "Cannot refund order with status: " + order.getOrderStatus() +
                            ". Order must be PAID, PROCESSING, or DELIVERED to refund.");
        }

        if (order.getRefundDate() != null) {
            throw new BadRequestException("Order has already been refunded on " + order.getRefundDate());
        }

        BigDecimal refundAmount = request.getRefundAmount();
        if (request.isFullRefund()) {
            refundAmount = order.getTotalAmount();
        }

        if (refundAmount.compareTo(order.getTotalAmount()) > 0) {
            throw new BadRequestException(
                    "Refund amount (" + refundAmount + ") cannot exceed order total (" +
                            order.getTotalAmount() + ")");
        }

        User adminUser = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminUserId));

        order.setRefundAmount(refundAmount);
        order.setRefundReason(request.getReason());
        order.setRefundDate(LocalDateTime.now());
        order.setRefundedBy(adminUser);
        order.setOrderStatus(OrderStatus.REFUNDED);
        order.setPaymentStatus(PaymentStatus.REFUNDED);

        Order savedOrder = orderRepository.save(order);

        logger.info("Refund processed for order {}: amount={}, reason={}",
                orderId, refundAmount, request.getReason());

        String adminName = adminUser.getFirstName() + " " + adminUser.getLastName();
        return RefundResponse.success(savedOrder, adminName);
    }

    @Transactional(readOnly = true)
    public RefundResponse getRefundDetails(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getRefundDate() == null) {
            return RefundResponse.failed(order.getOrderNumber(), "Order has not been refunded");
        }

        String adminName = "";
        if (order.getRefundedBy() != null) {
            adminName = order.getRefundedBy().getFirstName() + " " +
                    order.getRefundedBy().getLastName();
        }

        return RefundResponse.success(order, adminName);
    }

    private static BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
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