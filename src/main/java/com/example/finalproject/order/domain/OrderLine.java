package com.example.finalproject.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name = "order_lines")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "price_snapshot", nullable = false)
    private Integer priceSnapshot;

    @Column(name = "product_name_snapshot", nullable = false)
    private String productNameSnapshot;

    @Column(nullable = false)
    private Integer quantity;

    @Builder
    public OrderLine(Order order, Long productId, Long storeId,
                     Integer priceSnapshot, String productNameSnapshot, Integer quantity) {
        this.order = order;
        this.productId = productId;
        this.storeId = storeId;
        this.priceSnapshot = priceSnapshot;
        this.productNameSnapshot = productNameSnapshot;
        this.quantity = quantity;
    }
}

