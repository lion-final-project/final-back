package com.example.finalproject.product.domain;

import com.example.finalproject.global.domain.BaseTimeEntity;
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
@Table(name = "product_stock_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductStockHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_stock_history_product"))
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 10)
    private StockEventType eventType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "stock_after", nullable = false)
    private Integer stockAfter;

    @Builder
    public ProductStockHistory(Product product, StockEventType eventType,
                               Integer quantity, Integer stockAfter) {
        this.product = product;
        this.eventType = eventType;
        this.quantity = quantity;
        this.stockAfter = stockAfter;
    }

    public static ProductStockHistory createInHistory(Product product, Integer quantity, Integer stockAfter) {
        return ProductStockHistory.builder()
                .product(product)
                .eventType(StockEventType.IN)
                .quantity(quantity)
                .stockAfter(stockAfter)
                .build();
    }

    public static ProductStockHistory createOutHistory(Product product, Integer quantity, Integer stockAfter) {
        return ProductStockHistory.builder()
                .product(product)
                .eventType(StockEventType.OUT)
                .quantity(quantity)
                .stockAfter(stockAfter)
                .build();
    }
}
