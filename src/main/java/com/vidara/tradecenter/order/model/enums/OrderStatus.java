package com.vidara.tradecenter.order.model.enums;

public enum OrderStatus {

    PENDING,
    PAID,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus newStatus) {
        if (newStatus == CANCELLED) {
            return this != DELIVERED && this != CANCELLED;
        }

        return switch (this) {
            case PENDING -> newStatus == PAID;
            case PAID -> newStatus == PROCESSING;
            case PROCESSING -> newStatus == SHIPPED;
            case SHIPPED -> newStatus == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}