package com.vidara.tradecenter.payment.repository;

import com.vidara.tradecenter.payment.model.PayHereNotifyReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PayHereNotifyReceiptRepository extends JpaRepository<PayHereNotifyReceipt, Long> {

    boolean existsByPaymentId(String paymentId);

    Optional<PayHereNotifyReceipt> findByPaymentId(String paymentId);
}
