package com.vidara.tradecenter.support.dto.response;

import com.vidara.tradecenter.support.model.SupportTicket;
import com.vidara.tradecenter.support.model.enums.TicketCategory;
import com.vidara.tradecenter.support.model.enums.TicketPriority;
import com.vidara.tradecenter.support.model.enums.TicketStatus;

import java.time.LocalDateTime;

public class TicketResponse {

    private Long ticketId;
    private String subject;
    private TicketCategory category;
    private TicketStatus status;
    private TicketPriority priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long messageCount;
    private String lastMessage;


    // CONSTRUCTORS

    public TicketResponse() {
    }

    public TicketResponse(Long ticketId, String subject, TicketCategory category,
                          TicketStatus status, TicketPriority priority,
                          LocalDateTime createdAt, LocalDateTime updatedAt,
                          long messageCount, String lastMessage) {
        this.ticketId = ticketId;
        this.subject = subject;
        this.category = category;
        this.status = status;
        this.priority = priority;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.messageCount = messageCount;
        this.lastMessage = lastMessage;
    }


    // STATIC FACTORY METHOD

    public static TicketResponse fromEntity(SupportTicket ticket, long messageCount, String lastMessage) {
        return new TicketResponse(
                ticket.getId(),
                ticket.getSubject(),
                ticket.getCategory(),
                ticket.getStatus(),
                ticket.getPriority(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                messageCount,
                lastMessage
        );
    }


    // GETTERS AND SETTERS

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public TicketCategory getCategory() {
        return category;
    }

    public void setCategory(TicketCategory category) {
        this.category = category;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(long messageCount) {
        this.messageCount = messageCount;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
}
