package com.vidara.tradecenter.order.service;

import com.vidara.tradecenter.common.exception.BadRequestException;
import com.vidara.tradecenter.common.exception.ResourceNotFoundException;
import com.vidara.tradecenter.order.dto.DeliveryStatusResponse;
import com.vidara.tradecenter.order.model.DeliveryTracking;
import com.vidara.tradecenter.order.model.Order;
import com.vidara.tradecenter.order.model.enums.DeliveryStatus;
import com.vidara.tradecenter.order.model.enums.OrderStatus;
import com.vidara.tradecenter.order.repository.DeliveryTrackingRepository;
import com.vidara.tradecenter.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class DeliveryTrackingService {

    private static final Logger logger = LoggerFactory.getLogger(DeliveryTrackingService.class);

    private final DeliveryTrackingRepository deliveryTrackingRepository;
    private final OrderRepository orderRepository;

    public DeliveryTrackingService(DeliveryTrackingRepository deliveryTrackingRepository,
                                   OrderRepository orderRepository) {
        this.deliveryTrackingRepository = deliveryTrackingRepository;
        this.orderRepository = orderRepository;
    }


    // ===== CUSTOMER METHODS =====

    /**
     * Get delivery status for a specific order (customer view)
     */
    @Transactional(readOnly = true)
    public DeliveryStatusResponse getDeliveryStatus(Long userId, String orderNumber) {
        logger.info("User {} fetching delivery status for order {}", userId, orderNumber);

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));

        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Order does not belong to this user");
        }

        Optional<DeliveryTracking> tracking = deliveryTrackingRepository.findByOrderId(order.getId());

        if (tracking.isPresent()) {
            return DeliveryStatusResponse.fromEntity(tracking.get());
        } else {
            return DeliveryStatusResponse.notStarted(orderNumber);
        }
    }

    /**
     * Get delivery status by order ID (customer view)
     */
    @Transactional(readOnly = true)
    public DeliveryStatusResponse getDeliveryStatusByOrderId(Long userId, Long orderId) {
        logger.info("User {} fetching delivery status for order ID {}", userId, orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new BadRequestException("Order does not belong to this user");
        }

        Optional<DeliveryTracking> tracking = deliveryTrackingRepository.findByOrderId(orderId);

        if (tracking.isPresent()) {
            return DeliveryStatusResponse.fromEntity(tracking.get());
        } else {
            return DeliveryStatusResponse.notStarted(order.getOrderNumber());
        }
    }

    /**
     * Get all delivery trackings for a user
     */
    @Transactional(readOnly = true)
    public List<DeliveryStatusResponse> getUserDeliveries(Long userId) {
        logger.info("Fetching all deliveries for user {}", userId);

        return deliveryTrackingRepository.findByUserId(userId).stream()
                .map(DeliveryStatusResponse::fromEntity)
                .toList();
    }


    // ===== ADMIN METHODS =====

    /**
     * Get delivery tracking by order ID (admin view - no user ownership check)
     */
    @Transactional(readOnly = true)
    public DeliveryStatusResponse getDeliveryTrackingByOrderId(Long orderId) {
        logger.info("Admin fetching delivery tracking for order {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        Optional<DeliveryTracking> tracking = deliveryTrackingRepository.findByOrderId(orderId);

        if (tracking.isPresent()) {
            return DeliveryStatusResponse.fromEntity(tracking.get());
        } else {
            return DeliveryStatusResponse.notStarted(order.getOrderNumber());
        }
    }

    /**
     * Create delivery tracking for an order (admin)
     */
    @Transactional
    public DeliveryStatusResponse createDeliveryTracking(Long orderId,
                                                         String trackingNumber,
                                                         String courierName,
                                                         LocalDate estimatedDeliveryDate) {
        logger.info("Creating delivery tracking for order {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (deliveryTrackingRepository.existsByOrderId(orderId)) {
            throw new BadRequestException("Delivery tracking already exists for this order");
        }

        if (order.getOrderStatus() == OrderStatus.PENDING ||
                order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cannot create delivery tracking for " +
                    order.getOrderStatus() + " orders");
        }

        DeliveryTracking tracking = new DeliveryTracking();
        tracking.setOrder(order);
        tracking.setStatus(DeliveryStatus.PREPARING);
        tracking.setTrackingNumber(trackingNumber);
        tracking.setCourierName(courierName);
        tracking.setEstimatedDeliveryDate(estimatedDeliveryDate);

        DeliveryTracking saved = deliveryTrackingRepository.save(tracking);
        logger.info("Created delivery tracking {} for order {}", saved.getId(), orderId);

        return DeliveryStatusResponse.fromEntity(saved);
    }

    /**
     * Update delivery status (admin)
     */
    @Transactional
    public DeliveryStatusResponse updateDeliveryStatus(Long orderId,
                                                       DeliveryStatus newStatus,
                                                       String notes) {
        logger.info("Updating delivery status for order {} to {}", orderId, newStatus);

        DeliveryTracking tracking = deliveryTrackingRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "DeliveryTracking", "orderId", orderId));

        if (!tracking.canUpdateStatus(newStatus)) {
            throw new BadRequestException("Invalid delivery status transition: " +
                    tracking.getStatus() + " → " + newStatus);
        }

        tracking.setStatus(newStatus);
        if (notes != null && !notes.trim().isEmpty()) {
            tracking.setNotes(notes);
        }

        if (newStatus == DeliveryStatus.DELIVERED) {
            tracking.setActualDeliveryDate(LocalDate.now());

            Order order = tracking.getOrder();
            if (order.getOrderStatus().canTransitionTo(OrderStatus.DELIVERED)) {
                order.setOrderStatus(OrderStatus.DELIVERED);
                orderRepository.save(order);
            }
        }

        DeliveryTracking saved = deliveryTrackingRepository.save(tracking);
        logger.info("Updated delivery tracking {} status to {}", saved.getId(), newStatus);

        return DeliveryStatusResponse.fromEntity(saved);
    }

    /**
     * Update tracking information (admin)
     */
    @Transactional
    public DeliveryStatusResponse updateTrackingInfo(Long orderId,
                                                     String trackingNumber,
                                                     String courierName,
                                                     LocalDate estimatedDeliveryDate) {
        logger.info("Updating tracking info for order {}", orderId);

        DeliveryTracking tracking = deliveryTrackingRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "DeliveryTracking", "orderId", orderId));

        if (trackingNumber != null) {
            tracking.setTrackingNumber(trackingNumber);
        }
        if (courierName != null) {
            tracking.setCourierName(courierName);
        }
        if (estimatedDeliveryDate != null) {
            tracking.setEstimatedDeliveryDate(estimatedDeliveryDate);
        }

        DeliveryTracking saved = deliveryTrackingRepository.save(tracking);
        logger.info("Updated tracking info for order {}", orderId);

        return DeliveryStatusResponse.fromEntity(saved);
    }

    /**
     * Get delivery tracking by tracking ID (admin)
     */
    @Transactional(readOnly = true)
    public DeliveryStatusResponse getDeliveryTrackingById(Long trackingId) {
        DeliveryTracking tracking = deliveryTrackingRepository.findById(trackingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "DeliveryTracking", "id", trackingId));
        return DeliveryStatusResponse.fromEntity(tracking);
    }

    /**
     * Get overdue deliveries (admin)
     */
    @Transactional(readOnly = true)
    public List<DeliveryStatusResponse> getOverdueDeliveries() {
        logger.info("Fetching overdue deliveries");
        return deliveryTrackingRepository.findOverdueDeliveries(LocalDate.now()).stream()
                .map(DeliveryStatusResponse::fromEntity)
                .toList();
    }

    /**
     * Get deliveries by status (admin)
     */
    @Transactional(readOnly = true)
    public List<DeliveryStatusResponse> getDeliveriesByStatus(DeliveryStatus status) {
        logger.info("Fetching deliveries with status {}", status);
        return deliveryTrackingRepository.findByStatus(status).stream()
                .map(DeliveryStatusResponse::fromEntity)
                .toList();
    }
}