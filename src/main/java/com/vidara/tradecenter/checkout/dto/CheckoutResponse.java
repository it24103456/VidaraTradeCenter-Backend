package com.vidara.tradecenter.checkout.dto;

import java.math.BigDecimal;

public class CheckoutResponse {

    private String orderNumber;
    private BigDecimal totalAmount;

    public CheckoutResponse() {
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
