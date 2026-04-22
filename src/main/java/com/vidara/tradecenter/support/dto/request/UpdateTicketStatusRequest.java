package com.vidara.tradecenter.support.dto.request;

import com.vidara.tradecenter.support.model.enums.TicketStatus;
import jakarta.validation.constraints.NotNull;

public class UpdateTicketStatusRequest {

    @NotNull(message = "Status is required")
    private TicketStatus status;

    private String adminNote;


    // CONSTRUCTORS

    public UpdateTicketStatusRequest() {
    }

    public UpdateTicketStatusRequest(TicketStatus status, String adminNote) {
        this.status = status;
        this.adminNote = adminNote;
    }


    // GETTERS AND SETTERS

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }

    public String getAdminNote() {
        return adminNote;
    }

    public void setAdminNote(String adminNote) {
        this.adminNote = adminNote;
    }
}
