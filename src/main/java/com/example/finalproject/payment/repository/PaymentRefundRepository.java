package com.example.finalproject.payment.repository;

import com.example.finalproject.payment.domain.PaymentRefund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRefundRepository extends JpaRepository<PaymentRefund, Long> {
    @Query("SELECT COALESCE(SUM(pr.refundAmount), 0 ) "
            + "FROM PaymentRefund pr "
            + "WHERE pr.payment.id = :paymentId")
    int sumRefundAmountByPaymentId(@Param("paymentId") Long paymentId);
}

