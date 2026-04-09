package com.vidara.tradecenter.order.model.enums;

public enum DeliveryStatus {

    PREPARING("Order is being prepared"),
    SHIPPED("Order has been shipped"),
    IN_TRANSIT("Order is in transit"),
    OUT_FOR_DELIVERY("Order is out for delivery"),
    DELIVERED("Order has been delivered"),
    RETURNED("Order has been returned"),
    FAILED("Delivery failed");

    private final String description;

    DeliveryStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if delivery status can transition to a new status
     */
    public boolean canTransitionTo(DeliveryStatus newStatus) {
        if (newStatus == RETURNED || newStatus == FAILED) {
            return this != DELIVERED && this != RETURNED && this != FAILED;
        }

        return switch (this) {
            case PREPARING -> newStatus == SHIPPED;
            case SHIPPED -> newStatus == IN_TRANSIT;
            case IN_TRANSIT -> newStatus == OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> newStatus == DELIVERED;
            case DELIVERED, RETURNED, FAILED -> false;
        };
    }
}