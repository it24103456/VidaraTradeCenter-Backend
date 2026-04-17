package com.vidara.tradecenter.support.dto;

import com.vidara.tradecenter.support.model.enums.TicketCategory;

public class SupportTicketRequest {

    private String subject;
    private TicketCategory category;
    private String description;


    // CONSTRUCTORS

    public SupportTicketRequest() {
    }

    public SupportTicketRequest(String subject, TicketCategory category, String description) {
        this.subject = subject;
        this.category = category;
        this.description = description;
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
}
