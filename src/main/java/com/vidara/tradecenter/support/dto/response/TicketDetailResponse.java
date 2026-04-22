package com.vidara.tradecenter.support.dto.response;

import com.vidara.tradecenter.support.model.SupportTicket;
import com.vidara.tradecenter.support.model.enums.TicketCategory;
import com.vidara.tradecenter.support.model.enums.TicketPriority;
import com.vidara.tradecenter.support.model.enums.TicketStatus;

import java.time.LocalDateTime;
import java.util.List;

public class TicketDetailResponse {

    private Long ticketId;
    private String subject;
    private String description;
    private TicketCategory category;
    private TicketStatus status;
    private TicketPriority priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long messageCount;
    private String lastMessage;
    private String customerName;
    private String customerEmail;
    private String assignedAdmin;
    private List<TicketMessageResponse> messages;


    // CONSTRUCTORS

    public TicketDetailResponse() {
    }


    // STATIC FACTORY METHOD

    public static TicketDetailResponse fromEntity(SupportTicket ticket,
                                                   List<TicketMessageResponse> messages,
                                                   String assignedAdmin) {
        TicketDetailResponse response = new TicketDetailResponse();
        response.setTicketId(ticket.getId());
        response.setSubject(ticket.getSubject());
        response.setDescription(ticket.getDescription());
        response.setCategory(ticket.getCategory());
        response.setStatus(ticket.getStatus());
        response.setPriority(ticket.getPriority());
        response.setCreatedAt(ticket.getCreatedAt());
        response.setUpdatedAt(ticket.getUpdatedAt());
        response.setMessageCount(messages != null ? messages.size() : 0);
        response.setLastMessage(messages != null && !messages.isEmpty()
                ? messages.get(messages.size() - 1).getMessage() : null);
        response.setCustomerName(ticket.getUser().getFullName());
        response.setCustomerEmail(ticket.getUser().getEmail());
        response.setAssignedAdmin(assignedAdmin);
        response.setMessages(messages);
        return response;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getAssignedAdmin() {
        return assignedAdmin;
    }

    public void setAssignedAdmin(String assignedAdmin) {
        this.assignedAdmin = assignedAdmin;
    }

    public List<TicketMessageResponse> getMessages() {
        return messages;
    }

    public void setMessages(List<TicketMessageResponse> messages) {
        this.messages = messages;
    }
}
