package com.example.finalproject.payment.repository;

import com.example.finalproject.payment.domain.PaymentRefund;
import com.example.finalproject.payment.enums.RefundResponsibility;
import com.example.finalproject.payment.enums.RefundStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRefundRepository extends JpaRepository<PaymentRefund, Long> {
    List<PaymentRefund> findByStoreOrderIdOrderByCreatedAtDesc(Long storeOrderId);

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

    @Query("SELECT pr.storeOrder.id, COALESCE(SUM(pr.refundAmount), 0) "
            + "FROM PaymentRefund pr "
            + "WHERE pr.storeOrder.id IN :storeOrderIds "
            + "GROUP BY pr.storeOrder.id")
    List<Object[]> sumRefundAmountGroupByStoreOrderId(@Param("storeOrderIds") List<Long> storeOrderIds);

    @Query("SELECT COALESCE(SUM(pr.refundAmount), 0) "
            + "FROM PaymentRefund pr "
            + "WHERE pr.storeOrder.store.id = :storeId "
            + "AND pr.refundedAt > :startExclusive "
            + "AND pr.refundedAt <= :endInclusive "
            + "AND (pr.refundStatus = :approvedStatus OR pr.refundStatus IS NULL) "
            + "AND (pr.responsibility = :storeResponsibility OR pr.responsibility IS NULL)")
    long sumApprovedStoreRefundAmountByStoreIdAndCutoffBetween(
            @Param("storeId") Long storeId,
            @Param("startExclusive") LocalDateTime startExclusive,
            @Param("endInclusive") LocalDateTime endInclusive,
            @Param("approvedStatus") RefundStatus approvedStatus,
            @Param("storeResponsibility") RefundResponsibility storeResponsibility
    );

    long countByRefundStatus(RefundStatus refundStatus);

    @Query("SELECT COALESCE(SUM(pr.refundAmount), 0) "
            + "FROM PaymentRefund pr "
            + "WHERE pr.refundStatus = :refundStatus "
            + "AND pr.refundedAt BETWEEN :start AND :end")
    long sumRefundAmountByRefundStatusAndRefundedAtBetween(
            @Param("refundStatus") RefundStatus refundStatus,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}

