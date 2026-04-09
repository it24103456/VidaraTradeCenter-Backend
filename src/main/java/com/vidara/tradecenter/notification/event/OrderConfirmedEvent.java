package com.vidara.tradecenter.notification.event;

import com.vidara.tradecenter.notification.dto.OrderConfirmationEmail;
import org.springframework.context.ApplicationEvent;

public class OrderConfirmedEvent extends ApplicationEvent {

    private final OrderConfirmationEmail emailData;

    public OrderConfirmedEvent(Object source, OrderConfirmationEmail emailData) {
        super(source);
        this.emailData = emailData;
    }

    public OrderConfirmationEmail getEmailData() {
        return emailData;
    }
}
