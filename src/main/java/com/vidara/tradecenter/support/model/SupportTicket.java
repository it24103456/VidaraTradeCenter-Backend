package com.vidara.tradecenter.support.model;

import com.vidara.tradecenter.common.base.BaseEntity;
import com.vidara.tradecenter.support.model.enums.TicketCategory;
import com.vidara.tradecenter.support.model.enums.TicketPriority;
import com.vidara.tradecenter.support.model.enums.TicketStatus;
import com.vidara.tradecenter.user.model.User;
import jakarta.persistence.*;

@Entity
@Table(name = "support_tickets", indexes = {
        @Index(name = "idx_support_ticket_user", columnList = "user_id"),
        @Index(name = "idx_support_ticket_status", columnList = "status")
})
public class SupportTicket extends BaseEntity {

    @Column(name = "subject", nullable = false, length = 255)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private TicketCategory category;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TicketStatus status = TicketStatus.OPEN;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private TicketPriority priority = TicketPriority.MEDIUM;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    // CONSTRUCTORS

    public SupportTicket() {
    }

    public SupportTicket(String subject, TicketCategory category, String description, User user) {
        this.subject = subject;
        this.category = category;
        this.description = description;
        this.user = user;
        this.status = TicketStatus.OPEN;
        this.priority = TicketPriority.MEDIUM;
    }


    // GETTERS AND SETTERS

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    // TOSTRING

    @Override
    public String toString() {
        return "SupportTicket{" +
                "id=" + getId() +
                ", subject='" + subject + '\'' +
                ", category=" + category +
                ", status=" + status +
                ", priority=" + priority +
                ", userId=" + (user != null ? user.getId() : null) +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}
