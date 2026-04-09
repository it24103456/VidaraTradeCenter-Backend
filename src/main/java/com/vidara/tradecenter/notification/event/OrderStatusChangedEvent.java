package com.vidara.tradecenter.notification.event;

import com.vidara.tradecenter.notification.dto.OrderStatusUpdateEmail;
import org.springframework.context.ApplicationEvent;

public class OrderStatusChangedEvent extends ApplicationEvent {

    private final OrderStatusUpdateEmail emailData;

    public OrderStatusChangedEvent(Object source, OrderStatusUpdateEmail emailData) {
        super(source);
        this.emailData = emailData;
    }

    public OrderStatusUpdateEmail getEmailData() {
        return emailData;
    }
}
