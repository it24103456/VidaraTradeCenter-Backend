package com.vidara.tradecenter.payment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PaymentException extends RuntimeException {

    private String orderNumber;

    public PaymentException(String message) {
        super(message);
    }

    public PaymentException(String message, String orderNumber) {
        super(message);
        this.orderNumber = orderNumber;
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getOrderNumber() {
        return orderNumber;
    }
}
