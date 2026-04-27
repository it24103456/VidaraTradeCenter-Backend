package com.vidara.tradecenter.notification.service.impl;

import com.vidara.tradecenter.notification.config.EmailConfig;
import com.vidara.tradecenter.notification.dto.OrderConfirmationEmail;
import com.vidara.tradecenter.notification.dto.OrderStatusUpdateEmail;
import com.vidara.tradecenter.notification.dto.PasswordResetEmail;
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

            log.info("[ORDER_MAIL] SUCCESS confirmation sent order={} to={}",
                    emailData.getOrderNumber(), emailData.getCustomerEmail());
        } catch (Exception e) {
            log.error("[ORDER_MAIL] SMTP FAILED confirmation order={} to={} error={}",
                    emailData.getOrderNumber(), emailData.getCustomerEmail(), e.getMessage(), e);
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
            case "PREPARING" -> "We're preparing your order for shipment.";
            case "IN_TRANSIT" -> "Your package is in transit.";
            case "OUT_FOR_DELIVERY" -> "Your order is out for delivery and should arrive soon.";
            case "RETURNED" -> "A delivery return has been recorded for your order. Our team may contact you.";
            case "FAILED" -> "We ran into an issue with delivery. Please contact support if you need help.";
            default -> "Your order status has been updated to " + status + ".";
        };
    }

    @Override
    public void sendTicketConfirmation(String toEmail, String ticketId, String subject) {
        try {
            Context ctx = new Context();
            ctx.setVariable("ticketId", ticketId);
            ctx.setVariable("subject", subject);

            String html = templateEngine.process("email/ticket-confirmation", ctx);
            sendHtmlEmail(toEmail,
                    "Support Ticket Received - #" + ticketId, html);

            log.info("Ticket confirmation email sent to {} for ticket #{}", toEmail, ticketId);
        } catch (Exception e) {
            log.error("Failed to send ticket confirmation email for ticket #{}: {}",
                    ticketId, e.getMessage(), e);
        }
    }

    @Override
    public void sendTicketReply(String toEmail, String ticketId, String adminMessage) {
        try {
            Context ctx = new Context();
            ctx.setVariable("ticketId", ticketId);
            ctx.setVariable("adminMessage", adminMessage);

            String html = templateEngine.process("email/ticket-reply", ctx);
            sendHtmlEmail(toEmail,
                    "New Reply on Your Support Ticket #" + ticketId, html);

            log.info("Ticket reply email sent to {} for ticket #{}", toEmail, ticketId);
        } catch (Exception e) {
            log.error("Failed to send ticket reply email for ticket #{}: {}",
                    ticketId, e.getMessage(), e);
        }
    }

    @Override
    public void sendPasswordResetEmail(PasswordResetEmail emailData) {
        try {
            Context ctx = new Context();
            ctx.setVariable("customerName", emailData.getCustomerName());
            ctx.setVariable("resetLink", emailData.getResetLink());

            String html = templateEngine.process("email/password-reset", ctx);
            sendHtmlEmail(emailData.getCustomerEmail(),
                    "Password Reset Request - Vidara Trade Center", html);

            log.info("[PASSWORD_RESET] SUCCESS email sent to={}", emailData.getCustomerEmail());
        } catch (Exception e) {
            log.error("[PASSWORD_RESET] SMTP FAILED to={} error={}", emailData.getCustomerEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
}
