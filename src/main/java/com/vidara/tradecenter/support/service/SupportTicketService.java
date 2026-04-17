package com.vidara.tradecenter.support.service;

import com.vidara.tradecenter.common.exception.BadRequestException;
import com.vidara.tradecenter.common.exception.ResourceNotFoundException;
import com.vidara.tradecenter.support.dto.SupportTicketRequest;
import com.vidara.tradecenter.support.dto.SupportTicketResponse;
import com.vidara.tradecenter.support.model.SupportTicket;
import com.vidara.tradecenter.support.model.enums.TicketStatus;
import com.vidara.tradecenter.support.repository.SupportTicketRepository;
import com.vidara.tradecenter.user.model.User;
import com.vidara.tradecenter.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;
    private final UserRepository userRepository;

    public SupportTicketService(SupportTicketRepository supportTicketRepository,
                                UserRepository userRepository) {
        this.supportTicketRepository = supportTicketRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public SupportTicketResponse submitTicket(Long userId, SupportTicketRequest ticketData) {

        // Validate required fields
        if (ticketData.getSubject() == null || ticketData.getSubject().trim().isEmpty()) {
            throw new BadRequestException("Subject is required");
        }
        if (ticketData.getCategory() == null) {
            throw new BadRequestException("Category is required");
        }
        if (ticketData.getDescription() == null || ticketData.getDescription().trim().isEmpty()) {
            throw new BadRequestException("Description is required");
        }

        // Find the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Create and save the ticket
        SupportTicket ticket = new SupportTicket();
        ticket.setSubject(ticketData.getSubject().trim());
        ticket.setCategory(ticketData.getCategory());
        ticket.setDescription(ticketData.getDescription().trim());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setUser(user);

        SupportTicket savedTicket = supportTicketRepository.save(ticket);

        return SupportTicketResponse.fromEntity(savedTicket);
    }
}
