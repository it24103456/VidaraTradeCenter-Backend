package com.vidara.tradecenter.order.dto;

import com.vidara.tradecenter.order.model.DeliveryTracking;
import com.vidara.tradecenter.order.model.enums.DeliveryStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DeliveryStatusResponse {

    private Long id;
    private String orderNumber;
    private DeliveryStatus status;
    private String statusDescription;
    private LocalDate estimatedDeliveryDate;
    private LocalDate actualDeliveryDate;
    private String trackingNumber;
    private String courierName;
    private String notes;
    private boolean isDelivered;
    private LocalDateTime lastUpdated;


    // CONSTRUCTORS
    public DeliveryStatusResponse() {
    }


    // STATIC FACTORY METHOD
    public static DeliveryStatusResponse fromEntity(DeliveryTracking tracking) {
        if (tracking == null) {
            return null;
        }

        DeliveryStatusResponse response = new DeliveryStatusResponse();
        response.setId(tracking.getId());
        response.setOrderNumber(tracking.getOrder() != null ?
                tracking.getOrder().getOrderNumber() : null);
        response.setStatus(tracking.getStatus());
        response.setStatusDescription(tracking.getStatus() != null ?
                tracking.getStatus().getDescription() : null);
        response.setEstimatedDeliveryDate(tracking.getEstimatedDeliveryDate());
        response.setActualDeliveryDate(tracking.getActualDeliveryDate());
        response.setTrackingNumber(tracking.getTrackingNumber());
        response.setCourierName(tracking.getCourierName());
        response.setNotes(tracking.getNotes());
        response.setDelivered(tracking.isDelivered());
        response.setLastUpdated(tracking.getUpdatedAt());

        return response;
    }

    /**
     * Create a "not found" response when no tracking exists yet
     */
    public static DeliveryStatusResponse notStarted(String orderNumber) {
        DeliveryStatusResponse response = new DeliveryStatusResponse();
        response.setOrderNumber(orderNumber);
        response.setStatus(DeliveryStatus.PREPARING);
        response.setStatusDescription("Delivery tracking not yet available");
        response.setDelivered(false);
        return response;
    }


    // GETTERS AND SETTERS
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public void setStatus(DeliveryStatus status) {
        this.status = status;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
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

    public boolean isDelivered() {
        return isDelivered;
    }

    public void setDelivered(boolean delivered) {
        isDelivered = delivered;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}