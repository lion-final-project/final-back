package com.example.finalproject.subscription.domain;

import com.example.finalproject.global.domain.BaseTimeEntity;
import com.example.finalproject.product.domain.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subscription_product_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_sub_items_product",
                columnNames = {"subscription_product_id", "product_id"}
        ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubscriptionProductItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_product_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_sub_items_sub_product"))
    private SubscriptionProduct subscriptionProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_sub_items_product"))
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Builder
    public SubscriptionProductItem(SubscriptionProduct subscriptionProduct,
                                   Product product, Integer quantity) {
        this.subscriptionProduct = subscriptionProduct;
        this.product = product;
        this.quantity = quantity;
    }
}