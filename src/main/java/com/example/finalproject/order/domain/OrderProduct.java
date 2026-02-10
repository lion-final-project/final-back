package com.example.finalproject.order.domain;

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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderProduct extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_order_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_products_store_order"))
    private StoreOrder storeOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_products_product"))
    private Product product;

    @Column(name = "product_name_snapshot", nullable = false, length = 200)
    private String productNameSnapshot;

    @Column(name = "price_snapshot", nullable = false)
    private Integer priceSnapshot;

    @Column(nullable = false)
    private Integer quantity;

    @Builder
    public OrderProduct(StoreOrder storeOrder, Product product,
                        String productNameSnapshot, Integer priceSnapshot,
                        Integer quantity) {
        this.storeOrder = storeOrder;
        this.product = product;
        this.productNameSnapshot = productNameSnapshot;
        this.priceSnapshot = priceSnapshot;
        this.quantity = quantity;
    }

    public static OrderProduct of(StoreOrder storeOrder, Product product, OrderLine line) {
        return OrderProduct.builder()
                .storeOrder(storeOrder)
                .product(product)
                .productNameSnapshot(line.getProductNameSnapshot())
                .priceSnapshot(line.getPriceSnapshot())
                .quantity(line.getQuantity())
                .build();
    }

}