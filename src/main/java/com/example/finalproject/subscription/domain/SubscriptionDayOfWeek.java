package com.example.finalproject.subscription.domain;

import com.example.finalproject.global.domain.BaseTimeEntity;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subscription_day_of_week")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscriptionDayOfWeek extends BaseTimeEntity {

    @EmbeddedId
    private SubscriptionDayOfWeekId id;

    @MapsId("subscriptionId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id",
            foreignKey = @ForeignKey(name = "fk_sub_day_subscription"))
    private Subscription subscription;

    @Builder
    public SubscriptionDayOfWeek(Subscription subscription, Short dayOfWeek) {
        this.id = new SubscriptionDayOfWeekId(subscription.getId(), dayOfWeek);
        this.subscription = subscription;
    }
}
