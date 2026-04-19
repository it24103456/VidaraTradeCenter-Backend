package com.vidara.tradecenter.support.service.impl;

import com.vidara.tradecenter.common.exception.BadRequestException;
import com.vidara.tradecenter.common.exception.ResourceNotFoundException;
import com.vidara.tradecenter.notification.service.EmailNotificationService;
import com.vidara.tradecenter.support.dto.SupportTicketRequest;
import com.vidara.tradecenter.support.dto.SupportTicketResponse;
import com.vidara.tradecenter.support.dto.request.AddTicketMessageRequest;
import com.vidara.tradecenter.support.dto.request.CreateTicketRequest;
import com.vidara.tradecenter.support.dto.response.TicketMessageResponse;
import com.vidara.tradecenter.support.dto.response.TicketResponse;
import com.vidara.tradecenter.support.model.SupportTicket;
import com.vidara.tradecenter.support.model.TicketMessage;
import com.vidara.tradecenter.support.model.enums.TicketPriority;
import com.vidara.tradecenter.support.model.enums.TicketStatus;
import com.vidara.tradecenter.support.repository.SupportTicketRepository;
import com.vidara.tradecenter.support.repository.TicketMessageRepository;
import com.vidara.tradecenter.support.service.SupportTicketService;
import com.vidara.tradecenter.user.model.User;
import com.vidara.tradecenter.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SupportTicketServiceImpl implements SupportTicketService {

    private static final Logger log = LoggerFactory.getLogger(SupportTicketServiceImpl.class);

    private final SupportTicketRepository supportTicketRepository;
    private final TicketMessageRepository ticketMessageRepository;
    private final UserRepository userRepository;
    private final EmailNotificationService emailNotificationService;

    public SupportTicketServiceImpl(SupportTicketRepository supportTicketRepository,
                                    TicketMessageRepository ticketMessageRepository,
                                    UserRepository userRepository,
                                    EmailNotificationService emailNotificationService) {
        this.supportTicketRepository = supportTicketRepository;
        this.ticketMessageRepository = ticketMessageRepository;
        this.userRepository = userRepository;
        this.emailNotificationService = emailNotificationService;
    }


    @Override
    @Transactional
    public TicketResponse createTicket(Long userId, CreateTicketRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        SupportTicket ticket = new SupportTicket();
        ticket.setSubject(request.getSubject().trim());
        ticket.setCategory(request.getCategory());
        ticket.setDescription(request.getDescription().trim());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setPriority(request.getPriority() != null ? request.getPriority() : TicketPriority.MEDIUM);
        ticket.setUser(user);

        SupportTicket saved = supportTicketRepository.save(ticket);

        // Send ticket confirmation email
        try {
            emailNotificationService.sendTicketConfirmation(
                    user.getEmail(),
                    String.valueOf(saved.getId()),
                    saved.getSubject()
            );
        } catch (Exception e) {
            log.error("Failed to send ticket confirmation email for ticket {}: {}",
                    saved.getId(), e.getMessage());
        }

        return TicketResponse.fromEntity(saved, 0, null);
    }


    @Override
    @Transactional(readOnly = true)
    public List<TicketResponse> getMyTickets(Long userId) {
        List<SupportTicket> tickets = supportTicketRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return tickets.stream().map(ticket -> {
            long msgCount = ticketMessageRepository.countByTicketId(ticket.getId());
            List<TicketMessage> messages = ticketMessageRepository.findByTicketIdOrderBySentAt(ticket.getId());
            String lastMsg = messages.isEmpty() ? null : messages.get(messages.size() - 1).getMessage();
            return TicketResponse.fromEntity(ticket, msgCount, lastMsg);
        }).toList();
    }


    @Override
    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long userId, Long ticketId) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("SupportTicket", "id", ticketId));

        // Validate user ownership
        if (!ticket.getUser().getId().equals(userId)) {
            throw new BadRequestException("You do not have permission to view this ticket");
        }

        long msgCount = ticketMessageRepository.countByTicketId(ticketId);
        List<TicketMessage> messages = ticketMessageRepository.findByTicketIdOrderBySentAt(ticketId);
        String lastMsg = messages.isEmpty() ? null : messages.get(messages.size() - 1).getMessage();

        return TicketResponse.fromEntity(ticket, msgCount, lastMsg);
    }


    @Override
    @Transactional
    public TicketMessageResponse addMessage(Long userId, Long ticketId, AddTicketMessageRequest request) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("SupportTicket", "id", ticketId));

        // Validate user ownership
        if (!ticket.getUser().getId().equals(userId)) {
            throw new BadRequestException("You do not have permission to add messages to this ticket");
        }

        // Cannot add messages to closed tickets
        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new BadRequestException("Cannot add messages to a closed ticket");
        }

        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Determine if this is an admin reply
        boolean isAdmin = sender.getRoles().stream()
                .anyMatch(role -> "ROLE_ADMIN".equals(role.getName().getAuthority()));

        TicketMessage message = new TicketMessage(ticket, sender, request.getMessage().trim(), isAdmin);
        TicketMessage savedMessage = ticketMessageRepository.save(message);

        return TicketMessageResponse.fromEntity(savedMessage);
    }


    @Override
    @Transactional
    public TicketResponse closeTicket(Long userId, Long ticketId) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("SupportTicket", "id", ticketId));

        // Validate user ownership
        if (!ticket.getUser().getId().equals(userId)) {
            throw new BadRequestException("You do not have permission to close this ticket");
        }

        // Status transition check
        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new BadRequestException("Ticket is already closed");
        }

        ticket.setStatus(TicketStatus.CLOSED);
        SupportTicket saved = supportTicketRepository.save(ticket);

        long msgCount = ticketMessageRepository.countByTicketId(ticketId);
        List<TicketMessage> messages = ticketMessageRepository.findByTicketIdOrderBySentAt(ticketId);
        String lastMsg = messages.isEmpty() ? null : messages.get(messages.size() - 1).getMessage();

        return TicketResponse.fromEntity(saved, msgCount, lastMsg);
    }


    @Override
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
        ticket.setPriority(ticketData.getPriority() != null ? ticketData.getPriority() : TicketPriority.MEDIUM);
        ticket.setUser(user);

        SupportTicket savedTicket = supportTicketRepository.save(ticket);

        // Send ticket confirmation email
        try {
            emailNotificationService.sendTicketConfirmation(
                    user.getEmail(),
                    String.valueOf(savedTicket.getId()),
                    savedTicket.getSubject()
            );
        } catch (Exception e) {
            log.error("Failed to send ticket confirmation email for ticket {}: {}",
                    savedTicket.getId(), e.getMessage());
        }

        return SupportTicketResponse.fromEntity(savedTicket);
    }
}
