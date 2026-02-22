package com.example.finalproject.subscription.domain;

import com.example.finalproject.subscription.enums.SubscriptionStatus;
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 구독 상태 변경 이력 (BR-C10-05).
 * UC-C10 구독 해지/재개/해지 예정 등 상태 변경 시 기록한다.
 */
@Entity
@Table(name = "subscription_status_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscriptionStatusLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_sub_status_log_subscription"))
    private Subscription subscription;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 30)
    private SubscriptionStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 30)
    private SubscriptionStatus toStatus;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public SubscriptionStatusLog(Subscription subscription, SubscriptionStatus fromStatus,
                                  SubscriptionStatus toStatus, LocalDateTime createdAt) {
        this.subscription = subscription;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }
}
