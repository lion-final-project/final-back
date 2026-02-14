package com.example.finalproject.subscription.domain;


import com.example.finalproject.global.domain.BaseTimeEntity;
import com.example.finalproject.store.domain.Store;
import com.example.finalproject.subscription.enums.SubscriptionProductStatus;
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

@Entity
@Table(name = "subscription_products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscriptionProduct extends BaseTimeEntity {

    /** 구독 주기: 7일 × 4주 = 28일 */
    public static final int SUBSCRIPTION_PERIOD_DAYS = 28;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_sub_products_store"))
    private Store store;

    @Column(name = "subscription_product_name", nullable = false, length = 200)
    private String subscriptionProductName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer price;

    @Column(name = "total_delivery_count", nullable = false)
    private Integer totalDeliveryCount;

    @Column(name = "delivery_count_of_week", nullable = false)
    private Integer deliveryCountOfWeek;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionProductStatus status = SubscriptionProductStatus.ACTIVE;

    /**
     * 구독 상품 대표 이미지 URL.
     * DB 컬럼명은 하위 호환을 위해 subscription_url을 그대로 사용한다.
     */
    @Column(name = "subscription_url", length = 500)
    private String imageUrl;

    @Builder
    public SubscriptionProduct(Store store, String subscriptionProductName,
                              String description, Integer price, Integer totalDeliveryCount,
                              Integer deliveryCountOfWeek, String imageUrl) {
        this.store = store;
        this.subscriptionProductName = subscriptionProductName;
        this.description = description;
        this.price = price;
        this.totalDeliveryCount = totalDeliveryCount;
        this.deliveryCountOfWeek = deliveryCountOfWeek != null ? deliveryCountOfWeek : 1;
        this.imageUrl = imageUrl;
    }

    /**
     * 구독 상품 정보를 수정한다 (API-SOP-010P). 상태(status)는 별도 API(API-SOP-010S)로만 변경한다.
     *
     * @param name                 구독 상품명
     * @param description          설명
     * @param price                가격
     * @param totalDeliveryCount   월 총 배송 횟수
     * @param deliveryCountOfWeek  주당 배송 횟수 (null이면 totalDeliveryCount 기반 계산)
     * @param imageUrl             대표 이미지 URL (null이면 기존 값 유지)
     */
    public void updateDetails(String name, String description, Integer price,
                              Integer totalDeliveryCount, Integer deliveryCountOfWeek,
                              String imageUrl) {
        if (name != null) {
            this.subscriptionProductName = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (price != null) {
            this.price = price;
        }
        if (totalDeliveryCount != null) {
            this.totalDeliveryCount = totalDeliveryCount;
        }
        if (deliveryCountOfWeek != null) {
            this.deliveryCountOfWeek = deliveryCountOfWeek;
        } else if (totalDeliveryCount != null && totalDeliveryCount >= 4) {
            this.deliveryCountOfWeek = Math.max(1, totalDeliveryCount / 4);
        }
        if (imageUrl != null) {
            this.imageUrl = imageUrl;
        }
    }

    /**
     * 구독 상품 노출 상태를 변경한다 (API-SOP-010S).
     * ACTIVE: 노출(운영중), INACTIVE: 숨김.
     *
     * @param status 변경할 상태 (ACTIVE, INACTIVE)
     */
    public void updateStatus(SubscriptionProductStatus status) {
        this.status = status;
    }
}