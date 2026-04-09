package com.vidara.tradecenter.payment.model;

import com.vidara.tradecenter.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Records processed PayHere server notifications so the same {@code payment_id} is not applied twice.
 * Rows are only created when {@code payment_id} is non-empty (globally unique per PayHere).
 */
@Entity
@Table(name = "payhere_notify_receipts", indexes = {
        @Index(name = "idx_payhere_receipt_order", columnList = "order_number")
})
public class PayHereNotifyReceipt extends BaseEntity {

    @Column(name = "order_number", nullable = false, length = 64)
    private String orderNumber;

    /** PayHere payment_id; unique when present (multiple NULLs allowed for idempotency fallbacks on Order state). */
    @Column(name = "payment_id", unique = true, length = 128)
    private String paymentId;

    @Column(name = "status_code", nullable = false)
    private int statusCode;

    public PayHereNotifyReceipt() {
    }

    public PayHereNotifyReceipt(String orderNumber, String paymentId, int statusCode) {
        this.orderNumber = orderNumber;
        this.paymentId = paymentId;
        this.statusCode = statusCode;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
