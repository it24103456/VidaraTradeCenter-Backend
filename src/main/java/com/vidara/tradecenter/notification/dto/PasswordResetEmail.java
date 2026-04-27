package com.vidara.tradecenter.notification.dto;

public class PasswordResetEmail {

    private String customerEmail;
    private String customerName;
    private String resetLink;

    // CONSTRUCTORS

    public PasswordResetEmail() {
    }

    public PasswordResetEmail(String customerEmail, String customerName, String resetLink) {
        this.customerEmail = customerEmail;
        this.customerName = customerName;
        this.resetLink = resetLink;
    }

    // GETTERS AND SETTERS

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getResetLink() {
        return resetLink;
    }

    public void setResetLink(String resetLink) {
        this.resetLink = resetLink;
    }
}
