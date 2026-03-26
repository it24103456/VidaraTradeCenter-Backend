package com.vidara.tradecenter.payment.dto;

import jakarta.validation.constraints.NotBlank;

public class PaymentInitiateRequest {

    @NotBlank(message = "Order number is required")
    private String orderNumber;

    public PaymentInitiateRequest() {
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
}
