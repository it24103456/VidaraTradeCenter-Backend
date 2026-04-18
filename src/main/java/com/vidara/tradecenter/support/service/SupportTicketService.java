package com.vidara.tradecenter.support.service;

import com.vidara.tradecenter.support.dto.SupportTicketRequest;
import com.vidara.tradecenter.support.dto.SupportTicketResponse;
import com.vidara.tradecenter.support.dto.request.AddTicketMessageRequest;
import com.vidara.tradecenter.support.dto.request.CreateTicketRequest;
import com.vidara.tradecenter.support.dto.response.TicketMessageResponse;
import com.vidara.tradecenter.support.dto.response.TicketResponse;

import java.util.List;

public interface SupportTicketService {

    /**
     * Create a new support ticket (new DTO).
     */
    TicketResponse createTicket(Long userId, CreateTicketRequest request);

    /**
     * Get all tickets belonging to the authenticated user.
     */
    List<TicketResponse> getMyTickets(Long userId);

    /**
     * Get a single ticket by ID (validates user ownership).
     */
    TicketResponse getTicketById(Long userId, Long ticketId);

    /**
     * Add a message to an existing ticket.
     */
    TicketMessageResponse addMessage(Long userId, Long ticketId, AddTicketMessageRequest request);

    /**
     * Close a ticket (only the owner can close).
     */
    TicketResponse closeTicket(Long userId, Long ticketId);

    /**
     * Submit a support ticket (legacy method used by existing controller endpoint).
     */
    SupportTicketResponse submitTicket(Long userId, SupportTicketRequest ticketData);
}
