package com.vidara.tradecenter.order.repository;

import com.vidara.tradecenter.order.model.DeliveryTracking;
import com.vidara.tradecenter.order.model.enums.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryTrackingRepository extends JpaRepository<DeliveryTracking, Long> {

    // ===== BASIC METHODS =====

    Optional<DeliveryTracking> findByOrderId(Long orderId);

    Optional<DeliveryTracking> findByOrderOrderNumber(String orderNumber);

    Optional<DeliveryTracking> findByTrackingNumber(String trackingNumber);

    boolean existsByOrderId(Long orderId);


    // ===== STATUS QUERIES =====

    List<DeliveryTracking> findByStatus(DeliveryStatus status);

    List<DeliveryTracking> findByStatusIn(List<DeliveryStatus> statuses);

    long countByStatus(DeliveryStatus status);


    // ===== DATE-BASED QUERIES =====

    List<DeliveryTracking> findByEstimatedDeliveryDateBefore(LocalDate date);

    List<DeliveryTracking> findByEstimatedDeliveryDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT dt FROM DeliveryTracking dt WHERE dt.status != 'DELIVERED' " +
            "AND dt.estimatedDeliveryDate < :today")
    List<DeliveryTracking> findOverdueDeliveries(@Param("today") LocalDate today);


    // ===== USER-SPECIFIC QUERIES =====

    @Query("SELECT dt FROM DeliveryTracking dt WHERE dt.order.user.id = :userId " +
            "ORDER BY dt.updatedAt DESC")
    List<DeliveryTracking> findByUserId(@Param("userId") Long userId);

    @Query("SELECT dt FROM DeliveryTracking dt WHERE dt.order.user.id = :userId " +
            "AND dt.order.orderNumber = :orderNumber")
    Optional<DeliveryTracking> findByUserIdAndOrderNumber(
            @Param("userId") Long userId,
            @Param("orderNumber") String orderNumber);
}