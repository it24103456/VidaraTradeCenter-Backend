package com.vidara.tradecenter.support.dto.request;

import com.vidara.tradecenter.support.model.enums.TicketCategory;
import com.vidara.tradecenter.support.model.enums.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateTicketRequest {

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Description is required")
    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    @NotNull(message = "Category is required")
    private TicketCategory category;

    private TicketPriority priority = TicketPriority.MEDIUM;


    // CONSTRUCTORS

    public CreateTicketRequest() {
    }

    public CreateTicketRequest(String subject, String description, TicketCategory category, TicketPriority priority) {
        this.subject = subject;
        this.description = description;
        this.category = category;
        this.priority = priority != null ? priority : TicketPriority.MEDIUM;
    }


    // GETTERS AND SETTERS

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

    public TicketPriority getPriority() {
        return priority;
    }

    public void setPriority(TicketPriority priority) {
        this.priority = priority;
    }
}
