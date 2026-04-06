package com.vidara.tradecenter.order.service;

import com.vidara.tradecenter.common.dto.PagedResponse;
import com.vidara.tradecenter.order.dto.OrderHistoryFilterRequest;
import com.vidara.tradecenter.order.dto.OrderHistoryResponse;
import com.vidara.tradecenter.order.dto.OrderListResponse;
import com.vidara.tradecenter.order.dto.OrderResponse;
import com.vidara.tradecenter.order.dto.OrderStatusResponse;
import com.vidara.tradecenter.order.model.Order;
import com.vidara.tradecenter.order.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface OrderService {

    // Story 3.2 / 3.3 customer endpoints
    PagedResponse<OrderHistoryResponse> getOrderHistory(
            Long userId,
            OrderHistoryFilterRequest filterRequest,
            int page,
            int size);

    OrderResponse getOrderDetails(Long userId, Long orderId);

    OrderListResponse getOrderDetailsByOrderNumber(Long userId, String orderNumber);

    OrderStatusResponse getOrderStatus(Long userId, Long orderId);

    // Generic order service methods used by other flows
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
