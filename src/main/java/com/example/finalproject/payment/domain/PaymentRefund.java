package com.example.finalproject.payment.domain;


import com.example.finalproject.global.domain.BaseTimeEntity;
import com.example.finalproject.order.domain.StoreOrder;
import com.example.finalproject.payment.enums.RefundResponsibility;
import com.example.finalproject.payment.enums.RefundStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payment_refunds")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentRefund extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_refunds_payment"))
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_order_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_refunds_store_order"))
    private StoreOrder storeOrder;

    @Column(name = "refund_amount", nullable = false, precision = 12, scale = 2)
    private Integer refundAmount;

    @Column(name = "refund_reason", length = 500)
    private String refundReason;

    private LocalDateTime refundedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_responsibility", length = 30)
    private RefundResponsibility responsibility;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_status", nullable = false, length = 30)
    private RefundStatus refundStatus;

    @Column(name = "is_settled", nullable = false)
    private boolean isSettled = false;

    public void markSettled() {
        this.isSettled = true;
    }

    @Builder
    public PaymentRefund(Payment payment, StoreOrder storeOrder,
                         Integer refundAmount, String refundReason) {
        this.payment = payment;
        this.storeOrder = storeOrder;
        this.refundAmount = refundAmount;
        this.refundReason = refundReason;
        this.refundedAt = LocalDateTime.now();
    }
}
