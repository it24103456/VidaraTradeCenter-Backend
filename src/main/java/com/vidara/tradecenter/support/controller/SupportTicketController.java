package com.vidara.tradecenter.support.controller;

import com.vidara.tradecenter.common.dto.ApiResponse;
import com.vidara.tradecenter.security.CurrentUser;
import com.vidara.tradecenter.security.CustomUserDetails;
import com.vidara.tradecenter.support.dto.SupportTicketRequest;
import com.vidara.tradecenter.support.dto.SupportTicketResponse;
import com.vidara.tradecenter.support.service.SupportTicketService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/support")
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    public SupportTicketController(SupportTicketService supportTicketService) {
        this.supportTicketService = supportTicketService;
    }

    @PostMapping("/tickets")
    public ResponseEntity<ApiResponse<SupportTicketResponse>> submitTicket(
            @CurrentUser CustomUserDetails currentUser,
            @RequestBody SupportTicketRequest request) {

        SupportTicketResponse response = supportTicketService.submitTicket(
                currentUser.getId(), request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Support ticket submitted successfully", response));
    }
}
