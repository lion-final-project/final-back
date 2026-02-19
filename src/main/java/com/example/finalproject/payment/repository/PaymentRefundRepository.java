package com.example.finalproject.payment.repository;

import com.example.finalproject.payment.domain.PaymentRefund;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRefundRepository extends JpaRepository<PaymentRefund, Long> {
    @Query("SELECT COALESCE(SUM(pr.refundAmount), 0 ) "
            + "FROM PaymentRefund pr "
            + "WHERE pr.payment.id = :paymentId")
    int sumRefundAmountByPaymentId(@Param("paymentId") Long paymentId);

    @Query("SELECT COALESCE(SUM(pr.refundAmount), 0) "
            + "FROM PaymentRefund pr "
            + "WHERE pr.storeOrder.store.id = :storeId "
            + "AND pr.refundedAt BETWEEN :start AND :end")
    long sumRefundAmountByStoreOrderStoreIdAndRefundedAtBetween(
            @Param("storeId") Long storeId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /** 상점 기준 환불 금액 (배달비 제외, storeProductPrice만 합산) - 매출 조회용 */
    @Query("SELECT COALESCE(SUM(pr.storeOrder.storeProductPrice), 0L) "
            + "FROM PaymentRefund pr "
            + "WHERE pr.storeOrder.store.id = :storeId "
            + "AND pr.refundedAt BETWEEN :start AND :end")
    long sumStoreProductPriceByStoreOrderStoreIdAndRefundedAtBetween(
            @Param("storeId") Long storeId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}

