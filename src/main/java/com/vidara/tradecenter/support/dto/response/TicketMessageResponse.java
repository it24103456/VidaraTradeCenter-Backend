package com.vidara.tradecenter.support.dto.response;

import com.vidara.tradecenter.support.model.TicketMessage;

import java.time.LocalDateTime;

public class TicketMessageResponse {

    private Long messageId;
    private String senderName;
    private String message;
    private boolean isAdminReply;
    private LocalDateTime sentAt;


    // CONSTRUCTORS

    public TicketMessageResponse() {
    }

    public TicketMessageResponse(Long messageId, String senderName, String message,
                                  boolean isAdminReply, LocalDateTime sentAt) {
        this.messageId = messageId;
        this.senderName = senderName;
        this.message = message;
        this.isAdminReply = isAdminReply;
        this.sentAt = sentAt;
    }


    // STATIC FACTORY METHOD

    public static TicketMessageResponse fromEntity(TicketMessage msg) {
        return new TicketMessageResponse(
                msg.getId(),
                msg.getSender().getFullName(),
                msg.getMessage(),
                msg.isAdminReply(),
                msg.getSentAt()
        );
    }


    // GETTERS AND SETTERS

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
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
