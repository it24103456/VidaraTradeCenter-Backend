package com.vidara.tradecenter.notification.event;

import com.vidara.tradecenter.notification.service.EmailNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    private final EmailNotificationService emailNotificationService;

    public OrderEventListener(EmailNotificationService emailNotificationService) {
        this.emailNotificationService = emailNotificationService;
    }

    @Async
    @EventListener
    public void handleOrderConfirmed(OrderConfirmedEvent event) {
        var data = event.getEmailData();
        log.info("[ORDER_MAIL] Async handler: sending SMTP order={} to={}",
                data.getOrderNumber(), data.getCustomerEmail());
        try {
            emailNotificationService.sendOrderConfirmation(data);
            log.info("[ORDER_MAIL] Async handler finished (no exception) order={}", data.getOrderNumber());
        } catch (Exception e) {
            log.error("[ORDER_MAIL] Async handler exception order={}: {}",
                    data.getOrderNumber(), e.getMessage(), e);
        }
    }

    @Async
    @EventListener
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        log.info("Received OrderStatusChangedEvent for order {} ({}→{})",
                event.getEmailData().getOrderNumber(),
                event.getEmailData().getOldStatus(),
                event.getEmailData().getNewStatus());
        try {
            emailNotificationService.sendOrderStatusUpdate(event.getEmailData());
        } catch (Exception e) {
            log.error("Error handling OrderStatusChangedEvent for order {}: {}",
                    event.getEmailData().getOrderNumber(), e.getMessage(), e);
        }
    }
}
