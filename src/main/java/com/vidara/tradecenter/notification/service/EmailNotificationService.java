package com.vidara.tradecenter.notification.service;

import com.vidara.tradecenter.notification.dto.OrderConfirmationEmail;
import com.vidara.tradecenter.notification.dto.OrderStatusUpdateEmail;

public interface EmailNotificationService {

    void sendOrderConfirmation(OrderConfirmationEmail emailData);

    void sendOrderStatusUpdate(OrderStatusUpdateEmail emailData);

    void sendTicketConfirmation(String toEmail, String ticketId, String subject);

    void sendTicketReply(String toEmail, String ticketId, String adminMessage);
}
