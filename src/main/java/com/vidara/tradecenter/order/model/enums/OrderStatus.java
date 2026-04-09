package com.vidara.tradecenter.order.model.enums;

public enum OrderStatus {

    PENDING,
    PAID,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED;  // ← NEW

    public boolean canTransitionTo(OrderStatus newStatus) {
        // Cancelled and Refunded are terminal states
        if (this == CANCELLED || this == REFUNDED) {
            return false;
        }

        // Can cancel from most states (except delivered and already cancelled)
        if (newStatus == CANCELLED) {
            return this != DELIVERED && this != CANCELLED && this != REFUNDED;
        }

        // Can refund only from PAID, PROCESSING, DELIVERED states
        if (newStatus == REFUNDED) {
            return this == PAID || this == PROCESSING || this == DELIVERED;
        }

        return switch (this) {
            case PENDING -> newStatus == PAID || newStatus == CANCELLED;
            case PAID -> newStatus == PROCESSING;
            case PROCESSING -> newStatus == SHIPPED;
            case SHIPPED -> newStatus == DELIVERED;
            case DELIVERED -> newStatus == REFUNDED;  // Allow refund after delivery
            default -> false;
        };
    }

    /**
     * Check if order can be refunded from current status
     */
    public boolean canRefund() {
        return this == PAID || this == PROCESSING || this == DELIVERED;
    }
}