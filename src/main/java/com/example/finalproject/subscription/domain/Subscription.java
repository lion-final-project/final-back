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
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

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
                        Integer totalAmount, LocalDateTime startedAt) {
        this.user = user;
        this.store = store;
        this.subscriptionProduct = subscriptionProduct;
        this.address = address;
        this.paymentMethod = paymentMethod;
        this.totalAmount = totalAmount;
        this.startedAt = startedAt;
    }
}