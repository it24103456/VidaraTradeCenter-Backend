package com.vidara.tradecenter.payment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class PayHereException extends PaymentException {

    private String payHereErrorCode;

    public PayHereException(String message) {
        super(message);
    }

    public PayHereException(String message, String orderNumber) {
        super(message, orderNumber);
    }

    public PayHereException(String message, String orderNumber, String payHereErrorCode) {
        super(message, orderNumber);
        this.payHereErrorCode = payHereErrorCode;
    }

    public PayHereException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getPayHereErrorCode() {
        return payHereErrorCode;
    }
}
