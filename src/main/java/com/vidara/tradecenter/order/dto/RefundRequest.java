package com.vidara.tradecenter.order.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class RefundRequest {

    @NotNull(message = "Refund amount is required")
    @DecimalMin(value = "0.01", message = "Refund amount must be greater than 0")
    private BigDecimal refundAmount;

    @NotBlank(message = "Refund reason is required")
    @Size(min = 10, max = 500, message = "Refund reason must be between 10 and 500 characters")
    private String reason;

    private boolean fullRefund = false;

    private String notes;


    // CONSTRUCTORS
    public RefundRequest() {
    }

    public RefundRequest(BigDecimal refundAmount, String reason) {
        this.refundAmount = refundAmount;
        this.reason = reason;
    }


    // GETTERS AND SETTERS
    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public boolean isFullRefund() {
        return fullRefund;
    }

    public void setFullRefund(boolean fullRefund) {
        this.fullRefund = fullRefund;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }


    @Override
    public String toString() {
        return "RefundRequest{" +
                "refundAmount=" + refundAmount +
                ", reason='" + reason + '\'' +
                ", fullRefund=" + fullRefund +
                '}';
    }
}