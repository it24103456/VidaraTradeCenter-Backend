package com.vidara.tradecenter.support.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AddTicketMessageRequest {

    @NotBlank(message = "Message is required")
    @Size(max = 2000, message = "Message must not exceed 2000 characters")
    private String message;


    // CONSTRUCTORS

    public AddTicketMessageRequest() {
    }

    public AddTicketMessageRequest(String message) {
        this.message = message;
    }


    // GETTERS AND SETTERS

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
