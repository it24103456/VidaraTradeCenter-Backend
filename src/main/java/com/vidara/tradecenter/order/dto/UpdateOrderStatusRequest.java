package com.vidara.tradecenter.order.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateOrderStatusRequest {

    @NotBlank(message = "New status is required")
    private String newStatus;

    private String note;


    public UpdateOrderStatusRequest() {
    }

    public UpdateOrderStatusRequest(String newStatus, String note) {
        this.newStatus = newStatus;
        this.note = note;
    }


    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}