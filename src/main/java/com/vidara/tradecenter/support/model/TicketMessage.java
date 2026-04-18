package com.vidara.tradecenter.support.model;

import com.vidara.tradecenter.user.model.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_messages", indexes = {
        @Index(name = "idx_ticket_message_ticket", columnList = "ticket_id")
})
public class TicketMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private SupportTicket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_admin_reply", nullable = false)
    private boolean isAdminReply = false;

    @Column(name = "sent_at", nullable = false, updatable = false)
    private LocalDateTime sentAt;


    // AUTO-SET TIMESTAMP
    @PrePersist
    protected void onCreate() {
        this.sentAt = LocalDateTime.now();
    }


    // CONSTRUCTORS

    public TicketMessage() {
    }

    public TicketMessage(SupportTicket ticket, User sender, String message, boolean isAdminReply) {
        this.ticket = ticket;
        this.sender = sender;
        this.message = message;
        this.isAdminReply = isAdminReply;
    }


    // GETTERS AND SETTERS

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SupportTicket getTicket() {
        return ticket;
    }

    public void setTicket(SupportTicket ticket) {
        this.ticket = ticket;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isAdminReply() {
        return isAdminReply;
    }

    public void setAdminReply(boolean adminReply) {
        isAdminReply = adminReply;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

}
