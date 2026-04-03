package com.vidara.tradecenter.order.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class OrderNotFoundException extends RuntimeException {

    private String orderIdentifier;

    public OrderNotFoundException(String message) {
        super(message);
    }

    public OrderNotFoundException(String field, Object value) {
        super(String.format("Order not found with %s: '%s'", field, value));
        this.orderIdentifier = String.valueOf(value);
    }

    public OrderNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getOrderIdentifier() {
        return orderIdentifier;
    }
}
