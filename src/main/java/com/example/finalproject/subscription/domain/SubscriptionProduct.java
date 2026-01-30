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
    @Column(nullable = false, columnDefinition = "subscription_product_status DEFAULT 'ACTIVE'")
    private SubscriptionProductStatus status = SubscriptionProductStatus.ACTIVE;

    @Column(name = "subscription_url", length = 500)
    private String subscriptionUrl;

    @Builder
    public SubscriptionProduct(Store store, String subscriptionProductName,
                               Integer price, Integer totalDeliveryCount,
                               Integer deliveryCountOfWeek) {
        this.store = store;
        this.subscriptionProductName = subscriptionProductName;
        this.price = price;
        this.totalDeliveryCount = totalDeliveryCount;
        this.deliveryCountOfWeek = deliveryCountOfWeek;
    }
}