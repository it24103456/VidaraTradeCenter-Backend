package com.vidara.tradecenter.order.repository;

import com.vidara.tradecenter.order.model.Order;
import com.vidara.tradecenter.order.model.enums.OrderStatus;
import com.vidara.tradecenter.order.model.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // ===== BASIC METHODS (for Dev 1 & Dev 2) =====

    Optional<Order> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

    Page<Order> findByUserIdOrderByOrderDateDesc(Long userId, Pageable pageable);

    long countByOrderStatus(OrderStatus status);


    // ===== ADMIN METHODS (for Dev 3) =====

    @Query(value = "SELECT o FROM Order o JOIN o.user u WHERE " +
            "(:status IS NULL OR o.orderStatus = :status) AND " +
            "(:paymentStatus IS NULL OR o.paymentStatus = :paymentStatus) AND " +
            "(:search IS NULL OR LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:startDate IS NULL OR o.orderDate >= :startDate) AND " +
            "(:endDate IS NULL OR o.orderDate <= :endDate)",
            countQuery = "SELECT COUNT(o) FROM Order o JOIN o.user u WHERE " +
                    "(:status IS NULL OR o.orderStatus = :status) AND " +
                    "(:paymentStatus IS NULL OR o.paymentStatus = :paymentStatus) AND " +
                    "(:search IS NULL OR LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                    "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                    "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                    "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
                    "(:startDate IS NULL OR o.orderDate >= :startDate) AND " +
                    "(:endDate IS NULL OR o.orderDate <= :endDate)")
    Page<Order> findOrdersWithFilters(
            @Param("status") OrderStatus status,
            @Param("paymentStatus") PaymentStatus paymentStatus,
            @Param("search") String search,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    long countByPaymentStatus(PaymentStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o")
    BigDecimal sumTotalAmount();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderDate >= :date")
    BigDecimal sumTotalAmountAfter(@Param("date") LocalDateTime date);

    long countByOrderDateAfter(LocalDateTime date);
}