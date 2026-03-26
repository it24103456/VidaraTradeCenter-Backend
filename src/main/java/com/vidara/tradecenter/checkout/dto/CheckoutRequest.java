package com.vidara.tradecenter.checkout.dto;

import jakarta.validation.constraints.NotNull;

public class CheckoutRequest {

    @NotNull(message = "Shipping address ID is required")
    private Long shippingAddressId;

    public CheckoutRequest() {
    }

    public Long getShippingAddressId() {
        return shippingAddressId;
    }

    public void setShippingAddressId(Long shippingAddressId) {
        this.shippingAddressId = shippingAddressId;
    }
}
