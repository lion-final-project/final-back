package com.example.finalproject.order.domain;

import com.example.finalproject.global.domain.BaseTimeEntity;
import com.example.finalproject.product.domain.Product;
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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart_products",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_cart_products_cart_product",
                columnNames = {"cart_id", "product_id"}
        ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartProduct extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_cart_products_cart"))
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_cart_products_product"))
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_cart_products_store"))
    private Store store;

    @Column(nullable = false)
    private Integer quantity;

    @Builder
    public CartProduct(Cart cart, Product product, Store store, Integer quantity) {
        this.cart = cart;
        this.product = product;
        this.store = store;
        this.quantity = quantity;
    }

    public void changeQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("수량은 1 이상이어야 합니다.");
        }
        this.quantity = quantity;
    }

    public int getLinePrice() {
        return product.getEffectivePrice() * quantity;
    }
}