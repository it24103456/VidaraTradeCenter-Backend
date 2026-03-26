package com.vidara.tradecenter.checkout.controller;

import com.vidara.tradecenter.checkout.dto.CheckoutRequest;
import com.vidara.tradecenter.checkout.dto.CheckoutResponse;
import com.vidara.tradecenter.checkout.service.CheckoutService;
import com.vidara.tradecenter.common.dto.ApiResponse;
import com.vidara.tradecenter.security.CurrentUser;
import com.vidara.tradecenter.security.CustomUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;

    public CheckoutController(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    @PostMapping("/place-order")
    public ResponseEntity<ApiResponse<CheckoutResponse>> placeOrder(
            @CurrentUser CustomUserDetails currentUser,
            @Valid @RequestBody CheckoutRequest request) {
        CheckoutResponse response = checkoutService.placeOrder(currentUser.getId(), request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", response));
    }
}
