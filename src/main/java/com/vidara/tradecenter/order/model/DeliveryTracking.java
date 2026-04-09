package com.vidara.tradecenter.order.model;

import com.vidara.tradecenter.common.base.BaseEntity;
import com.vidara.tradecenter.order.model.enums.DeliveryStatus;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "delivery_tracking", indexes = {
        @Index(name = "idx_delivery_order", columnList = "order_id"),
        @Index(name = "idx_delivery_status", columnList = "status"),
        @Index(name = "idx_delivery_tracking_number", columnList = "tracking_number")
})
public class DeliveryTracking extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private DeliveryStatus status = DeliveryStatus.PREPARING;

    @Column(name = "estimated_delivery_date")
    private LocalDate estimatedDeliveryDate;

    @Column(name = "actual_delivery_date")
    private LocalDate actualDeliveryDate;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @Column(name = "courier_name", length = 100)
    private String courierName;

    @Column(name = "notes", length = 500)
    private String notes;


    // CONSTRUCTORS
    public DeliveryTracking() {
    }

    public DeliveryTracking(Order order) {
        this.order = order;
        this.status = DeliveryStatus.PREPARING;
    }

    public DeliveryTracking(Order order, DeliveryStatus status, LocalDate estimatedDeliveryDate) {
        this.order = order;
        this.status = status;
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }


    // HELPER METHODS
    public boolean isDelivered() {
        return this.status == DeliveryStatus.DELIVERED;
    }

    public boolean canUpdateStatus(DeliveryStatus newStatus) {
        return this.status.canTransitionTo(newStatus);
    }


    // GETTERS AND SETTERS
    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(DeliveryStatus status) {
        this.status = status;
    }

    public LocalDate getEstimatedDeliveryDate() {
        return estimatedDeliveryDate;
    }

    public void setEstimatedDeliveryDate(LocalDate estimatedDeliveryDate) {
        this.estimatedDeliveryDate = estimatedDeliveryDate;
    }

    public LocalDate getActualDeliveryDate() {
        return actualDeliveryDate;
    }

    public void setActualDeliveryDate(LocalDate actualDeliveryDate) {
        this.actualDeliveryDate = actualDeliveryDate;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }

    public String getCourierName() {
        return courierName;
    }

    public void setCourierName(String courierName) {
        this.courierName = courierName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }


    @Override
    public String toString() {
        return "DeliveryTracking{" +
                "id=" + getId() +
                ", orderId=" + (order != null ? order.getId() : null) +
                ", status=" + status +
                ", trackingNumber='" + trackingNumber + '\'' +
                ", estimatedDeliveryDate=" + estimatedDeliveryDate +
                '}';
    }
}