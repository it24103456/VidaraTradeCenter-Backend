package com.vidara.tradecenter.notification.dto;

public class OrderStatusUpdateEmail {

    private String customerName;
    private String customerEmail;
    private String orderNumber;
    private String oldStatus;
    private String newStatus;

    public OrderStatusUpdateEmail() {}

    public OrderStatusUpdateEmail(String customerName, String customerEmail,
                                  String orderNumber, String oldStatus, String newStatus) {
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.orderNumber = orderNumber;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public String getOldStatus() { return oldStatus; }
    public void setOldStatus(String oldStatus) { this.oldStatus = oldStatus; }
    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
}
