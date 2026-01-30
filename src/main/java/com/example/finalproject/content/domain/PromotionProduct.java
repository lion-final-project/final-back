package com.example.finalproject.content.domain;


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
@Table(name = "promotion_products",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_promo_products_unique",
                columnNames = {"promotion_id", "product_id"}
        ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PromotionProduct extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_promo_products_promotion"))
    private Promotion promotion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_promo_products_product"))
    private Product product;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Builder
    public PromotionProduct(Promotion promotion, Product product, Integer sortOrder) {
        this.promotion = promotion;
        this.product = product;
        this.sortOrder = sortOrder != null ? sortOrder : 0;
    }
}