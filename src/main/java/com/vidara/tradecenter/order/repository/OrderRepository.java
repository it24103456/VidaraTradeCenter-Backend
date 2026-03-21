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

    // ===== BASIC METHODS =====

    Optional<Order> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

    Page<Order> findByUserIdOrderByOrderDateDesc(Long userId, Pageable pageable);

    long countByOrderStatus(OrderStatus status);


    // ===== ADMIN: FILTERED LISTING (Native Query) =====

    @Query(value = "SELECT o.* FROM orders o INNER JOIN users u ON u.id = o.user_id WHERE " +
            "(CAST(:status AS TEXT) IS NULL OR o.order_status = CAST(:status AS TEXT)) AND " +
            "(CAST(:paymentStatus AS TEXT) IS NULL OR o.payment_status = CAST(:paymentStatus AS TEXT)) AND " +
            "(CAST(:search AS TEXT) IS NULL OR " +
            "LOWER(CAST(o.order_number AS TEXT)) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%')) OR " +
            "LOWER(CAST(u.first_name AS TEXT)) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%')) OR " +
            "LOWER(CAST(u.last_name AS TEXT)) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%')) OR " +
            "LOWER(CAST(u.email AS TEXT)) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%'))) AND " +
            "(CAST(:startDate AS TIMESTAMP) IS NULL OR o.order_date >= CAST(:startDate AS TIMESTAMP)) AND " +
            "(CAST(:endDate AS TIMESTAMP) IS NULL OR o.order_date <= CAST(:endDate AS TIMESTAMP))",
            countQuery = "SELECT COUNT(*) FROM orders o INNER JOIN users u ON u.id = o.user_id WHERE " +
                    "(CAST(:status AS TEXT) IS NULL OR o.order_status = CAST(:status AS TEXT)) AND " +
                    "(CAST(:paymentStatus AS TEXT) IS NULL OR o.payment_status = CAST(:paymentStatus AS TEXT)) AND " +
                    "(CAST(:search AS TEXT) IS NULL OR " +
                    "LOWER(CAST(o.order_number AS TEXT)) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%')) OR " +
                    "LOWER(CAST(u.first_name AS TEXT)) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%')) OR " +
                    "LOWER(CAST(u.last_name AS TEXT)) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%')) OR " +
                    "LOWER(CAST(u.email AS TEXT)) LIKE LOWER(CONCAT('%', CAST(:search AS TEXT), '%'))) AND " +
                    "(CAST(:startDate AS TIMESTAMP) IS NULL OR o.order_date >= CAST(:startDate AS TIMESTAMP)) AND " +
                    "(CAST(:endDate AS TIMESTAMP) IS NULL OR o.order_date <= CAST(:endDate AS TIMESTAMP))",
            nativeQuery = true)
    Page<Order> findOrdersWithFilters(
            @Param("status") String status,
            @Param("paymentStatus") String paymentStatus,
            @Param("search") String search,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);


    // ===== ADMIN: STATISTICS =====

    long countByPaymentStatus(PaymentStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o")
    BigDecimal sumTotalAmount();

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.orderDate >= :date")
    BigDecimal sumTotalAmountAfter(@Param("date") LocalDateTime date);

    long countByOrderDateAfter(LocalDateTime date);
}