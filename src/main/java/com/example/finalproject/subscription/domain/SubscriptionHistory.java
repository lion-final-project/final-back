package com.example.finalproject.subscription.domain;


import com.example.finalproject.global.domain.BaseTimeEntity;
import com.example.finalproject.order.domain.StoreOrder;
import com.example.finalproject.subscription.enums.SubHistoryStatus;
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subscription_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscriptionHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_sub_history_subscription"))
    private Subscription subscription;

    @Column(name = "cycle_count", nullable = false)
    private Integer cycleCount;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubHistoryStatus status = SubHistoryStatus.SCHEDULED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_order_id",
            foreignKey = @ForeignKey(name = "fk_sub_history_store_order"))
    private StoreOrder storeOrder;

    @Builder
    public SubscriptionHistory(Subscription subscription, Integer cycleCount,
                               LocalDate scheduledDate) {
        this.subscription = subscription;
        this.cycleCount = cycleCount;
        this.scheduledDate = scheduledDate;
    }

    /**
     * 생성된 StoreOrder를 연결하고 상태를 ORDERED로 변경한다 (구독 주문 자동 생성 후 호출).
     */
    public void linkStoreOrder(StoreOrder storeOrder) {
        this.storeOrder = storeOrder;
        this.status = SubHistoryStatus.ORDERED;
    }
}