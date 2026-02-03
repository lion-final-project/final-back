package com.example.finalproject.product.domain;

import com.example.finalproject.global.domain.BaseTimeEntity;
import com.example.finalproject.store.domain.Store;
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
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_products_store"))
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_products_category"))
    private Category category;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private Integer price;

    @Column(name = "sale_price", precision = 12, scale = 2)
    private Integer salePrice;

    @Column(name = "discount_rate", precision = 5, scale = 2)
    private BigDecimal discountRate;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(length = 30)
    private String unit;

    @Column(length = 100)
    private String origin;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "order_count", nullable = false)
    private Integer orderCount = 0;

    @Column(name = "product_image_url", length = 500)
    private String productImageUrl;

    @Builder
    public Product(Store store, Category category, String productName,
                   Integer price, Integer stock) {
        this.store = store;
        this.category = category;
        this.productName = productName;
        this.price = price;
        this.stock = stock != null ? stock : 0;
    }

    public int getEffectivePrice() {
        return salePrice != null ? salePrice : price;
    }

    public boolean isLowStock() {
        return stock < 5;
    }
}