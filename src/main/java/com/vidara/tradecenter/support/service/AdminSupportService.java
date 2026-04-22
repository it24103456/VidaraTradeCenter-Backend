package com.vidara.tradecenter.support.service;

import com.vidara.tradecenter.support.dto.request.AddTicketMessageRequest;
import com.vidara.tradecenter.support.dto.request.UpdateTicketStatusRequest;
import com.vidara.tradecenter.support.dto.response.TicketDetailResponse;
import com.vidara.tradecenter.support.dto.response.TicketMessageResponse;
import com.vidara.tradecenter.support.dto.response.TicketResponse;
import com.vidara.tradecenter.support.dto.response.TicketSummaryResponse;
import com.vidara.tradecenter.support.model.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminSupportService {

    /**
     * Get all tickets with optional status filter, paginated.
     */
    Page<TicketResponse> getAllTickets(Pageable pageable, TicketStatus status);

    /**
     * Get full ticket detail including message thread.
     */
    TicketDetailResponse getTicketDetail(Long ticketId);

    /**
     * Update ticket status with transition validation.
     * CLOSED → OPEN/IN_PROGRESS/RESOLVED is not allowed.
     */
    TicketResponse updateStatus(Long ticketId, UpdateTicketStatusRequest request);

    /**
     * Add an admin reply to a ticket and email the customer.
     */
    TicketMessageResponse addAdminReply(Long ticketId, AddTicketMessageRequest request, Long adminUserId);

    /**
     * Get ticket statistics (counts by status + urgent count).
     */
    TicketSummaryResponse getTicketStats();
}
