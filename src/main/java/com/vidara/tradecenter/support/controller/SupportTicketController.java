package com.vidara.tradecenter.support.controller;

import com.vidara.tradecenter.common.dto.ApiResponse;
import com.vidara.tradecenter.security.CurrentUser;
import com.vidara.tradecenter.security.CustomUserDetails;
import com.vidara.tradecenter.support.dto.SupportTicketRequest;
import com.vidara.tradecenter.support.dto.SupportTicketResponse;
import com.vidara.tradecenter.support.dto.request.AddTicketMessageRequest;
import com.vidara.tradecenter.support.dto.request.CreateTicketRequest;
import com.vidara.tradecenter.support.dto.response.TicketMessageResponse;
import com.vidara.tradecenter.support.dto.response.TicketResponse;
import com.vidara.tradecenter.support.service.SupportTicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/support")
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    public SupportTicketController(SupportTicketService supportTicketService) {
        this.supportTicketService = supportTicketService;
    }


    /**
     * POST /api/support/tickets
     * Submit a new support ticket (legacy endpoint).
     * Requires authenticated user (JWT token).
     */
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


    /**
     * GET /api/support/tickets
     * Get all tickets belonging to the authenticated user.
     */
    @GetMapping("/tickets")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getMyTickets(
            @CurrentUser CustomUserDetails currentUser) {

        List<TicketResponse> tickets = supportTicketService.getMyTickets(currentUser.getId());

        return ResponseEntity.ok(ApiResponse.success("Tickets retrieved successfully", tickets));
    }


    /**
     * GET /api/support/tickets/{id}
     * Get a single ticket by ID (validates user owns the ticket).
     */
    @GetMapping("/tickets/{id}")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicketById(
            @CurrentUser CustomUserDetails currentUser,
            @PathVariable Long id) {

        TicketResponse ticket = supportTicketService.getTicketById(currentUser.getId(), id);

        return ResponseEntity.ok(ApiResponse.success("Ticket retrieved successfully", ticket));
    }


    /**
     * POST /api/support/tickets/{id}/messages
     * Add a message to an existing ticket.
     */
    @PostMapping("/tickets/{id}/messages")
    public ResponseEntity<ApiResponse<TicketMessageResponse>> addMessage(
            @CurrentUser CustomUserDetails currentUser,
            @PathVariable Long id,
            @Valid @RequestBody AddTicketMessageRequest request) {

        TicketMessageResponse response = supportTicketService.addMessage(
                currentUser.getId(), id, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Message added successfully", response));
    }


    /**
     * PUT /api/support/tickets/{id}/close
     * Close a ticket (only the owner can close).
     */
    @PutMapping("/tickets/{id}/close")
    public ResponseEntity<ApiResponse<TicketResponse>> closeTicket(
            @CurrentUser CustomUserDetails currentUser,
            @PathVariable Long id) {

        TicketResponse response = supportTicketService.closeTicket(currentUser.getId(), id);

        return ResponseEntity.ok(ApiResponse.success("Ticket closed successfully", response));
    }
}
