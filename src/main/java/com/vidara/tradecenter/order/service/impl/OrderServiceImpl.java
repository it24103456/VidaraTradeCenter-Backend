package com.vidara.tradecenter.order.service.impl;

import com.vidara.tradecenter.common.dto.PagedResponse;
import com.vidara.tradecenter.common.exception.BadRequestException;
import com.vidara.tradecenter.common.exception.ForbiddenException;
import com.vidara.tradecenter.common.exception.ResourceNotFoundException;
import com.vidara.tradecenter.order.dto.OrderHistoryFilterRequest;
import com.vidara.tradecenter.order.dto.OrderHistoryResponse;
import com.vidara.tradecenter.order.dto.OrderListResponse;
import com.vidara.tradecenter.order.dto.OrderResponse;
import com.vidara.tradecenter.order.dto.OrderStatusResponse;
import com.vidara.tradecenter.order.exception.OrderNotFoundException;
import com.vidara.tradecenter.order.mapper.OrderMapper;
import com.vidara.tradecenter.order.model.Order;
import com.vidara.tradecenter.order.model.enums.OrderStatus;
import com.vidara.tradecenter.order.repository.OrderRepository;
import com.vidara.tradecenter.order.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    public OrderServiceImpl(OrderRepository orderRepository, OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<OrderHistoryResponse> getOrderHistory(
            Long userId,
            OrderHistoryFilterRequest filterRequest,
            int page,
            int size) {

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 50);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Order> orderPage;
        if (hasFilters(filterRequest)) {
            OrderStatus status = parseOrderStatus(filterRequest.getStatus());
            String search = normalize(filterRequest.getSearch());
            orderPage = orderRepository.findOrderHistoryWithFilters(
                    userId,
                    status,
                    search,
                    filterRequest.getStartDate(),
                    filterRequest.getEndDate(),
                    pageable);
        } else {
            orderPage = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        return PagedResponse.of(
                orderPage.getContent().stream().map(orderMapper::toHistoryResponse).toList(),
                orderPage
        );
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetails(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        return OrderResponse.fromEntity(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderListResponse getOrderDetailsByOrderNumber(Long userId, String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));

        if (!order.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Order does not belong to this user");
        }

        return OrderListResponse.fromEntityDetailed(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderStatusResponse getOrderStatus(Long userId, Long orderId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        return orderMapper.toStatusResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        logger.info("Fetching order by orderNumber: {}", orderNumber);
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("orderNumber", orderNumber));
        return OrderResponse.fromEntity(order);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        logger.info("Fetching order by id: {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("id", orderId));
        return OrderResponse.fromEntity(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderEntityByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("orderNumber", orderNumber));
    }

    @Override
    @Transactional(readOnly = true)
    public Order getOrderEntityById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("id", orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByUserId(Long userId, Pageable pageable) {
        logger.info("Fetching orders for user: {}, page: {}", userId, pageable.getPageNumber());
        Page<Order> orderPage = orderRepository.findByUserIdOrderByOrderDateDesc(userId, pageable);
        return orderPage.map(OrderResponse::fromEntity);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        logger.info("Updating order {} status to {}", orderId, newStatus);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("id", orderId));

        validateStatusTransition(order, newStatus);
        order.setOrderStatus(newStatus);
        Order saved = orderRepository.save(order);

        logger.info("Order {} status updated to {}", orderId, newStatus);
        return OrderResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatusByOrderNumber(String orderNumber, OrderStatus newStatus) {
        logger.info("Updating order {} status to {}", orderNumber, newStatus);

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new OrderNotFoundException("orderNumber", orderNumber));

        validateStatusTransition(order, newStatus);
        order.setOrderStatus(newStatus);
        Order saved = orderRepository.save(order);

        logger.info("Order {} status updated to {}", orderNumber, newStatus);
        return OrderResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    @Override
    public boolean existsByOrderNumber(String orderNumber) {
        return orderRepository.existsByOrderNumber(orderNumber);
    }

    private boolean hasFilters(OrderHistoryFilterRequest filterRequest) {
        return filterRequest != null
                && (filterRequest.getStartDate() != null
                || filterRequest.getEndDate() != null
                || normalize(filterRequest.getStatus()) != null
                || normalize(filterRequest.getSearch()) != null);
    }

    private String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private OrderStatus parseOrderStatus(String status) {
        String normalized = normalize(status);
        if (normalized == null) {
            return null;
        }

        try {
            return OrderStatus.valueOf(normalized.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid order status filter: " + status);
        }
    }

    private void validateStatusTransition(Order order, OrderStatus newStatus) {
        OrderStatus currentStatus = order.getOrderStatus();
        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new BadRequestException(
                    "Invalid status transition: " + currentStatus + " -> " + newStatus);
        }
    }
}
