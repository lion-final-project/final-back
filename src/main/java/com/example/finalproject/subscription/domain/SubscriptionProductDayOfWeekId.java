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
public class SubscriptionProductDayOfWeekId implements Serializable {

    @Column(name = "subscription_product_id")
    private Long subscriptionProductId;

    @Column(name = "day_of_week")
    private Short dayOfWeek;

    public SubscriptionProductDayOfWeekId(Long subscriptionProductId, Short dayOfWeek) {
        this.subscriptionProductId = subscriptionProductId;
        this.dayOfWeek = dayOfWeek;
    }
}
