package com.vidara.tradecenter.order.service;

import com.vidara.tradecenter.order.dto.OrderResponse;
import com.vidara.tradecenter.order.model.Order;
import com.vidara.tradecenter.order.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface OrderService {

    OrderResponse getOrderByOrderNumber(String orderNumber);

    OrderResponse getOrderById(Long orderId);

    Order getOrderEntityByOrderNumber(String orderNumber);

    Order getOrderEntityById(Long orderId);

    Page<OrderResponse> getOrdersByUserId(Long userId, Pageable pageable);

    OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus);

    OrderResponse updateOrderStatusByOrderNumber(String orderNumber, OrderStatus newStatus);

    Optional<Order> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);
}
