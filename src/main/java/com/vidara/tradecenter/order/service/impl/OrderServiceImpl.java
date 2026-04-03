package com.vidara.tradecenter.order.service.impl;

import com.vidara.tradecenter.order.dto.OrderResponse;
import com.vidara.tradecenter.order.exception.OrderNotFoundException;
import com.vidara.tradecenter.order.model.Order;
import com.vidara.tradecenter.order.model.enums.OrderStatus;
import com.vidara.tradecenter.order.repository.OrderRepository;
import com.vidara.tradecenter.order.service.OrderService;
import com.vidara.tradecenter.common.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
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

    private void validateStatusTransition(Order order, OrderStatus newStatus) {
        OrderStatus currentStatus = order.getOrderStatus();
        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new BadRequestException(
                    "Invalid status transition: " + currentStatus + " → " + newStatus);
        }
    }
}
