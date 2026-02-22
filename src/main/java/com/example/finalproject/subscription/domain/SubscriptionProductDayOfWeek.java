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

/**
 * 구독 상품별 배송 요일.
 * 마트 사장님이 구독 상품 등록/수정 시 설정한 배송 가능 요일을 저장한다.
 * day_of_week: 0=일, 1=월, 2=화, 3=수, 4=목, 5=금, 6=토
 */
@Entity
@Table(name = "subscription_product_day_of_week")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscriptionProductDayOfWeek extends BaseTimeEntity {

    @EmbeddedId
    private SubscriptionProductDayOfWeekId id;

    @MapsId("subscriptionProductId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_product_id",
            foreignKey = @ForeignKey(name = "fk_sub_product_day_sub_product"))
    private SubscriptionProduct subscriptionProduct;

    @Builder
    public SubscriptionProductDayOfWeek(SubscriptionProduct subscriptionProduct, Short dayOfWeek) {
        this.id = new SubscriptionProductDayOfWeekId(subscriptionProduct.getId(), dayOfWeek);
        this.subscriptionProduct = subscriptionProduct;
    }
}
