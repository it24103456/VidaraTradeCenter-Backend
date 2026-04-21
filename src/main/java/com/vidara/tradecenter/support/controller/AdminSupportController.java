package com.vidara.tradecenter.support.controller;

import com.vidara.tradecenter.common.dto.ApiResponse;
import com.vidara.tradecenter.security.CurrentUser;
import com.vidara.tradecenter.security.CustomUserDetails;
import com.vidara.tradecenter.support.dto.request.AddTicketMessageRequest;
import com.vidara.tradecenter.support.dto.request.UpdateTicketStatusRequest;
import com.vidara.tradecenter.support.dto.response.TicketDetailResponse;
import com.vidara.tradecenter.support.dto.response.TicketMessageResponse;
import com.vidara.tradecenter.support.dto.response.TicketResponse;
import com.vidara.tradecenter.support.dto.response.TicketSummaryResponse;
import com.vidara.tradecenter.support.model.enums.TicketStatus;
import com.vidara.tradecenter.support.service.AdminSupportService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/support")
public class AdminSupportController {

    private final AdminSupportService adminSupportService;

    public AdminSupportController(AdminSupportService adminSupportService) {
        this.adminSupportService = adminSupportService;
    }

    // Paginated list of all tickets with optional status filter.

    @GetMapping("/tickets")
    public ResponseEntity<ApiResponse<Page<TicketResponse>>> getAllTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) TicketStatus status) {

        Pageable pageable = PageRequest.of(page, size);
        Page<TicketResponse> tickets = adminSupportService.getAllTickets(pageable, status);

        return ResponseEntity.ok(ApiResponse.success("Tickets retrieved successfully", tickets));
    }


     // Full ticket detail including message thread.

    @GetMapping("/tickets/{id}")
    public ResponseEntity<ApiResponse<TicketDetailResponse>> getTicketDetail(
            @PathVariable Long id) {

        TicketDetailResponse detail = adminSupportService.getTicketDetail(id);

        return ResponseEntity.ok(ApiResponse.success("Ticket detail retrieved successfully", detail));
    }


     // Update ticket status with transition validation.

    @PutMapping("/tickets/{id}/status")
    public ResponseEntity<ApiResponse<TicketResponse>> updateTicketStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTicketStatusRequest request) {

        TicketResponse response = adminSupportService.updateStatus(id, request);

        return ResponseEntity.ok(ApiResponse.success("Ticket status updated successfully", response));
    }



     // Add admin reply and email the customer.

    @PostMapping("/tickets/{id}/reply")
    public ResponseEntity<ApiResponse<TicketMessageResponse>> addAdminReply(
            @PathVariable Long id,
            @CurrentUser CustomUserDetails currentUser,
            @Valid @RequestBody AddTicketMessageRequest request) {

        TicketMessageResponse response = adminSupportService.addAdminReply(
                id, request, currentUser.getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Admin reply added successfully", response));
    }



     // Ticket statistics (counts by status + urgent count).

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<TicketSummaryResponse>> getTicketStats() {

        TicketSummaryResponse stats = adminSupportService.getTicketStats();

        return ResponseEntity.ok(ApiResponse.success("Ticket statistics retrieved successfully", stats));
    }
}
