package com.example.finalproject.subscription.domain;


import com.example.finalproject.global.domain.BaseTimeEntity;
import com.example.finalproject.payment.domain.PaymentMethod;
import com.example.finalproject.store.domain.Store;
import com.example.finalproject.subscription.enums.SubscriptionStatus;
import com.example.finalproject.user.domain.Address;
import com.example.finalproject.user.domain.User;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subscriptions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subscription extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_subscriptions_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_subscriptions_store"))
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_product_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_subscriptions_sub_product"))
    private SubscriptionProduct subscriptionProduct;

    @Column(name = "delivery_time_slot", length = 30)
    private String deliveryTimeSlot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_subscriptions_address"))
    private Address address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_subscriptions_payment_method"))
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status = SubscriptionStatus.PENDING;

    @Column(name = "next_payment_date")
    private LocalDate nextPaymentDate;

    @Column(name = "total_amount", nullable = false)
    private Integer totalAmount;

    @Column(name = "cycle_count", nullable = false)
    private Integer cycleCount = 1;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    private LocalDateTime pausedAt;
    private LocalDateTime cancelledAt;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;


    @Builder
    public Subscription(User user, Store store, SubscriptionProduct subscriptionProduct,
                        Address address, PaymentMethod paymentMethod,
                        Integer totalAmount, LocalDateTime startedAt,
                        LocalDate nextPaymentDate, String deliveryTimeSlot,
                        SubscriptionStatus status) {
        this.user = user;
        this.store = store;
        this.subscriptionProduct = subscriptionProduct;
        this.address = address;
        this.paymentMethod = paymentMethod;
        this.totalAmount = totalAmount;
        this.startedAt = startedAt;
        this.nextPaymentDate = nextPaymentDate;
        this.deliveryTimeSlot = deliveryTimeSlot;
        this.status = status;
    }

    /**
     * 구독을 일시정지한다 (UC-C10). ACTIVE 상태에서만 호출 가능.
     */
    public void pause() {
        if (this.status != SubscriptionStatus.ACTIVE) {
            throw new IllegalStateException("ACTIVE 상태에서만 일시정지할 수 있습니다.");
        }
        this.status = SubscriptionStatus.PAUSED;
        this.pausedAt = LocalDateTime.now();
    }

    /**
     * 일시정지된 구독을 재개한다 (UC-C10). PAUSED 상태에서만 호출 가능.
     */
    public void resume() {
        if (this.status != SubscriptionStatus.PAUSED) {
            throw new IllegalStateException("PAUSED 상태에서만 재개할 수 있습니다.");
        }
        this.status = SubscriptionStatus.ACTIVE;
        this.pausedAt = null;
    }

    /**
     * 구독 해지를 요청한다 (UC-C10). 다음 결제일 기준 해지 정책에 따라 해지 예정 상태로 전환한다. ACTIVE 또는 PAUSED 상태에서만 호출 가능.
     *
     * @param reason 해지 사유 (선택)
     */
    public void requestCancellation(String reason) {
        if (this.status != SubscriptionStatus.ACTIVE && this.status != SubscriptionStatus.PAUSED) {
            throw new IllegalStateException("ACTIVE 또는 PAUSED 상태에서만 해지 요청할 수 있습니다.");
        }
        this.status = SubscriptionStatus.CANCELLATION_PENDING;
        this.cancelReason = reason;
        this.cancelledAt = LocalDateTime.now();
    }

    /**
     * 해지 예정을 취소하고 구독을 유지한다 (UC-C10 5-a). CANCELLATION_PENDING 상태에서만 호출 가능.
     */
    public void cancelCancellation() {
        if (this.status != SubscriptionStatus.CANCELLATION_PENDING) {
            throw new IllegalStateException("해지 예정 상태에서만 해지 취소할 수 있습니다.");
        }
        this.status = SubscriptionStatus.ACTIVE;
        this.cancelReason = null;
        this.cancelledAt = null;
    }

    public void activate() {
        this.status = SubscriptionStatus.ACTIVE;
    }

    public void markPaymentFailed() {
        this.status = SubscriptionStatus.PAYMENT_FAILED;
    }

    public void moveNextBillingDate() {
        this.nextPaymentDate = this.nextPaymentDate.plusDays(SubscriptionProduct.SUBSCRIPTION_PERIOD_DAYS);
    }

    /**
     * 배송 완료 시 주기 차수를 1 증가시킨다 (구독 진행 상황 추적용).
     */
    public void incrementCycleCount() {
        this.cycleCount = (this.cycleCount != null ? this.cycleCount : 1) + 1;
    }
}