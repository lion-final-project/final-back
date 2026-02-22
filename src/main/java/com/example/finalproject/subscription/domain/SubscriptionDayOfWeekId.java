package com.example.finalproject.subscription.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class SubscriptionDayOfWeekId implements Serializable {

    @Column(name = "subscription_id")
    private Long subscriptionId;

    @Column(name = "day_of_week")
    private Short dayOfWeek;

    public SubscriptionDayOfWeekId(Long subscriptionId, Short dayOfWeek) {
        this.subscriptionId = subscriptionId;
        this.dayOfWeek = dayOfWeek;
    }
}
