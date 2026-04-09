package com.vidara.tradecenter.notification.service.impl;

import com.vidara.tradecenter.notification.config.EmailConfig;
import com.vidara.tradecenter.notification.dto.OrderConfirmationEmail;
import com.vidara.tradecenter.notification.dto.OrderStatusUpdateEmail;
import com.vidara.tradecenter.notification.service.EmailNotificationService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailNotificationServiceImpl implements EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationServiceImpl.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final EmailConfig emailConfig;

    public EmailNotificationServiceImpl(JavaMailSender mailSender,
                                        TemplateEngine templateEngine,
                                        EmailConfig emailConfig) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.emailConfig = emailConfig;
    }

    @Override
    public void sendOrderConfirmation(OrderConfirmationEmail emailData) {
        try {
            Context ctx = new Context();
            ctx.setVariable("customerName", emailData.getCustomerName());
            ctx.setVariable("orderNumber", emailData.getOrderNumber());
            ctx.setVariable("orderDate", emailData.getOrderDate());
            ctx.setVariable("items", emailData.getItems());
            ctx.setVariable("subtotal", emailData.getSubtotal());
            ctx.setVariable("tax", emailData.getTax());
            ctx.setVariable("shippingCost", emailData.getShippingCost());
            ctx.setVariable("totalAmount", emailData.getTotalAmount());
            ctx.setVariable("shippingAddress", emailData.getShippingAddress());

            String html = templateEngine.process("email/order-confirmation", ctx);
            sendHtmlEmail(emailData.getCustomerEmail(),
                    "Order Confirmation - " + emailData.getOrderNumber(), html);

            log.info("Order confirmation email sent to {} for order {}",
                    emailData.getCustomerEmail(), emailData.getOrderNumber());
        } catch (Exception e) {
            log.error("Failed to send order confirmation email for order {}: {}",
                    emailData.getOrderNumber(), e.getMessage(), e);
        }
    }

    @Override
    public void sendOrderStatusUpdate(OrderStatusUpdateEmail emailData) {
        try {
            Context ctx = new Context();
            ctx.setVariable("customerName", emailData.getCustomerName());
            ctx.setVariable("orderNumber", emailData.getOrderNumber());
            ctx.setVariable("oldStatus", emailData.getOldStatus());
            ctx.setVariable("newStatus", emailData.getNewStatus());
            ctx.setVariable("statusMessage", buildStatusMessage(emailData.getNewStatus()));

            String html = templateEngine.process("email/order-status-update", ctx);
            sendHtmlEmail(emailData.getCustomerEmail(),
                    "Order Update - " + emailData.getOrderNumber() + " is now " + emailData.getNewStatus(), html);

            log.info("Order status update email sent to {} for order {} ({}→{})",
                    emailData.getCustomerEmail(), emailData.getOrderNumber(),
                    emailData.getOldStatus(), emailData.getNewStatus());
        } catch (Exception e) {
            log.error("Failed to send order status update email for order {}: {}",
                    emailData.getOrderNumber(), e.getMessage(), e);
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        try {
            InternetAddress from = new InternetAddress(
                    emailConfig.getFromAddress(),
                    emailConfig.getFromName());
            helper.setFrom(from);
        } catch (java.io.UnsupportedEncodingException e) {
            throw new MessagingException("Failed to encode From display name", e);
        }
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        mailSender.send(message);
    }

    private String buildStatusMessage(String status) {
        return switch (status.toUpperCase()) {
            case "PROCESSING" -> "We've started preparing your order for shipment.";
            case "SHIPPED" -> "Great news! Your order has been shipped and is on its way to you.";
            case "DELIVERED" -> "Your order has been delivered. We hope you enjoy your purchase!";
            case "CANCELLED" -> "Your order has been cancelled. If you have questions, please contact support.";
            case "PAID" -> "Your payment has been confirmed. We'll start processing your order soon.";
            default -> "Your order status has been updated to " + status + ".";
        };
    }
}
