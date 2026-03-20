package com.vidara.tradecenter.order.repository;

import com.vidara.tradecenter.order.model.Order;
import com.vidara.tradecenter.order.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

    Page<Order> findByUserIdOrderByOrderDateDesc(Long userId, Pageable pageable);

    long countByOrderStatus(OrderStatus status);
}