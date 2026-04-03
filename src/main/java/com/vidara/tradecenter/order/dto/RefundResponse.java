package com.vidara.tradecenter.order.dto;

import com.vidara.tradecenter.order.model.Order;
import com.vidara.tradecenter.order.model.enums.OrderStatus;
import com.vidara.tradecenter.order.model.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RefundResponse {

    private Long orderId;
    private String orderNumber;
    private BigDecimal originalAmount;
    private BigDecimal refundedAmount;
    private String refundReason;
    private LocalDateTime refundDate;
    private Long refundedBy;
    private String refundedByName;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private boolean success;
    private String message;


    // CONSTRUCTORS
    public RefundResponse() {
    }


    // STATIC FACTORY METHODS
    public static RefundResponse success(Order order, String adminName) {
        RefundResponse response = new RefundResponse();
        response.setOrderId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setOriginalAmount(order.getTotalAmount());
        response.setRefundedAmount(order.getRefundAmount());
        response.setRefundReason(order.getRefundReason());
        response.setRefundDate(order.getRefundDate());
        response.setRefundedBy(order.getRefundedBy() != null ? order.getRefundedBy().getId() : null);
        response.setRefundedByName(adminName);
        response.setOrderStatus(order.getOrderStatus());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setSuccess(true);
        response.setMessage("Refund processed successfully");
        return response;
    }

    public static RefundResponse failed(String orderNumber, String errorMessage) {
        RefundResponse response = new RefundResponse();
        response.setOrderNumber(orderNumber);
        response.setSuccess(false);
        response.setMessage(errorMessage);
        return response;
    }


    // GETTERS AND SETTERS
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }

    public BigDecimal getRefundedAmount() {
        return refundedAmount;
    }

    public void setRefundedAmount(BigDecimal refundedAmount) {
        this.refundedAmount = refundedAmount;
    }

    public String getRefundReason() {
        return refundReason;
    }

    public void setRefundReason(String refundReason) {
        this.refundReason = refundReason;
    }

    public LocalDateTime getRefundDate() {
        return refundDate;
    }

    public void setRefundDate(LocalDateTime refundDate) {
        this.refundDate = refundDate;
    }

    public Long getRefundedBy() {
        return refundedBy;
    }

    public void setRefundedBy(Long refundedBy) {
        this.refundedBy = refundedBy;
    }

    public String getRefundedByName() {
        return refundedByName;
    }

    public void setRefundedByName(String refundedByName) {
        this.refundedByName = refundedByName;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}