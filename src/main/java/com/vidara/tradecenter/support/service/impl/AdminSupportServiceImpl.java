package com.vidara.tradecenter.support.service.impl;

import com.vidara.tradecenter.common.exception.BadRequestException;
import com.vidara.tradecenter.common.exception.ResourceNotFoundException;
import com.vidara.tradecenter.notification.service.EmailNotificationService;
import com.vidara.tradecenter.support.dto.request.AddTicketMessageRequest;
import com.vidara.tradecenter.support.dto.request.UpdateTicketStatusRequest;
import com.vidara.tradecenter.support.dto.response.TicketDetailResponse;
import com.vidara.tradecenter.support.dto.response.TicketMessageResponse;
import com.vidara.tradecenter.support.dto.response.TicketResponse;
import com.vidara.tradecenter.support.dto.response.TicketSummaryResponse;
import com.vidara.tradecenter.support.model.SupportTicket;
import com.vidara.tradecenter.support.model.TicketMessage;
import com.vidara.tradecenter.support.model.enums.TicketPriority;
import com.vidara.tradecenter.support.model.enums.TicketStatus;
import com.vidara.tradecenter.support.repository.SupportTicketRepository;
import com.vidara.tradecenter.support.repository.TicketMessageRepository;
import com.vidara.tradecenter.support.service.AdminSupportService;
import com.vidara.tradecenter.user.model.User;
import com.vidara.tradecenter.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminSupportServiceImpl implements AdminSupportService {

    private static final Logger log = LoggerFactory.getLogger(AdminSupportServiceImpl.class);

    private final SupportTicketRepository supportTicketRepository;
    private final TicketMessageRepository ticketMessageRepository;
    private final UserRepository userRepository;
    private final EmailNotificationService emailNotificationService;

    public AdminSupportServiceImpl(SupportTicketRepository supportTicketRepository,
                                    TicketMessageRepository ticketMessageRepository,
                                    UserRepository userRepository,
                                    EmailNotificationService emailNotificationService) {
        this.supportTicketRepository = supportTicketRepository;
        this.ticketMessageRepository = ticketMessageRepository;
        this.userRepository = userRepository;
        this.emailNotificationService = emailNotificationService;
    }


    @Override
    @Transactional(readOnly = true)
    public Page<TicketResponse> getAllTickets(Pageable pageable, TicketStatus status) {
        Page<SupportTicket> ticketPage;

        if (status != null) {
            ticketPage = supportTicketRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            ticketPage = supportTicketRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        return ticketPage.map(ticket -> {
            long msgCount = ticketMessageRepository.countByTicketId(ticket.getId());
            List<TicketMessage> messages = ticketMessageRepository.findByTicketIdOrderBySentAt(ticket.getId());
            String lastMsg = messages.isEmpty() ? null : messages.get(messages.size() - 1).getMessage();
            return TicketResponse.fromEntity(ticket, msgCount, lastMsg);
        });
    }


    @Override
    @Transactional(readOnly = true)
    public TicketDetailResponse getTicketDetail(Long ticketId) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("SupportTicket", "id", ticketId));

        List<TicketMessage> messages = ticketMessageRepository.findByTicketIdOrderBySentAt(ticketId);
        List<TicketMessageResponse> messageResponses = messages.stream()
                .map(TicketMessageResponse::fromEntity)
                .toList();

        // Find the assigned admin (last admin who replied)
        String assignedAdmin = messages.stream()
                .filter(TicketMessage::isAdminReply)
                .reduce((first, second) -> second)
                .map(msg -> msg.getSender().getFullName())
                .orElse(null);

        return TicketDetailResponse.fromEntity(ticket, messageResponses, assignedAdmin);
    }


    @Override
    @Transactional
    public TicketResponse updateStatus(Long ticketId, UpdateTicketStatusRequest request) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("SupportTicket", "id", ticketId));

        TicketStatus currentStatus = ticket.getStatus();
        TicketStatus newStatus = request.getStatus();

        // Validate status transition: CLOSED tickets cannot be transitioned
        if (currentStatus == TicketStatus.CLOSED) {
            throw new BadRequestException(
                    "Cannot change status of a closed ticket. Current: CLOSED, Requested: " + newStatus);
        }

        ticket.setStatus(newStatus);
        SupportTicket saved = supportTicketRepository.save(ticket);

        // If admin provided a note, save it as a system message
        if (request.getAdminNote() != null && !request.getAdminNote().trim().isEmpty()) {
            log.info("Admin note for ticket #{}: {}", ticketId, request.getAdminNote());
        }

        long msgCount = ticketMessageRepository.countByTicketId(ticketId);
        List<TicketMessage> messages = ticketMessageRepository.findByTicketIdOrderBySentAt(ticketId);
        String lastMsg = messages.isEmpty() ? null : messages.get(messages.size() - 1).getMessage();

        return TicketResponse.fromEntity(saved, msgCount, lastMsg);
    }


    @Override
    @Transactional
    public TicketMessageResponse addAdminReply(Long ticketId, AddTicketMessageRequest request, Long adminUserId) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("SupportTicket", "id", ticketId));

        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminUserId));

        // Create admin reply message
        TicketMessage message = new TicketMessage(ticket, admin, request.getMessage().trim(), true);
        TicketMessage savedMessage = ticketMessageRepository.save(message);

        // If ticket is OPEN, move to IN_PROGRESS on first admin reply
        if (ticket.getStatus() == TicketStatus.OPEN) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
            supportTicketRepository.save(ticket);
        }

        // Send reply notification email to the customer
        try {
            emailNotificationService.sendTicketReply(
                    ticket.getUser().getEmail(),
                    String.valueOf(ticket.getId()),
                    request.getMessage().trim()
            );
        } catch (Exception e) {
            log.error("Failed to send ticket reply email for ticket #{}: {}",
                    ticketId, e.getMessage());
        }

        return TicketMessageResponse.fromEntity(savedMessage);
    }


    @Override
    @Transactional(readOnly = true)
    public TicketSummaryResponse getTicketStats() {
        long totalOpen = supportTicketRepository.countByStatus(TicketStatus.OPEN);
        long totalInProgress = supportTicketRepository.countByStatus(TicketStatus.IN_PROGRESS);
        long totalResolved = supportTicketRepository.countByStatus(TicketStatus.RESOLVED);
        long totalClosed = supportTicketRepository.countByStatus(TicketStatus.CLOSED);
        long urgentCount = supportTicketRepository.countByPriority(TicketPriority.URGENT);

        return new TicketSummaryResponse(totalOpen, totalInProgress, totalResolved, totalClosed, urgentCount);
    }
}
