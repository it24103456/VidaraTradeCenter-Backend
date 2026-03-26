package com.vidara.tradecenter.payment.controller;

import com.vidara.tradecenter.common.dto.ApiResponse;
import com.vidara.tradecenter.payment.dto.PaymentInitiateRequest;
import com.vidara.tradecenter.payment.dto.PaymentInitiateResponse;
import com.vidara.tradecenter.payment.service.PayHereService;
import com.vidara.tradecenter.security.CurrentUser;
import com.vidara.tradecenter.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PayHereService payHereService;

    public PaymentController(PayHereService payHereService) {
        this.payHereService = payHereService;
    }

    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<PaymentInitiateResponse>> initiate(
            @CurrentUser CustomUserDetails currentUser,
            @Valid @RequestBody PaymentInitiateRequest request,
            HttpServletRequest httpRequest) {

        String serverBaseUrl = httpRequest.getScheme() + "://" + httpRequest.getServerName()
                + ":" + httpRequest.getServerPort();

        PaymentInitiateResponse response = payHereService.initiatePayment(
                currentUser.getId(),
                request.getOrderNumber(),
                serverBaseUrl
        );

        return ResponseEntity.ok(ApiResponse.success("Payment initiated", response));
    }

    @PostMapping("/notify")
    public ResponseEntity<String> notify(HttpServletRequest request) {
        payHereService.handleNotification(request);
        return ResponseEntity.ok("OK");
    }
}
