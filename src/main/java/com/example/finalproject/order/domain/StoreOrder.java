package com.example.finalproject.order.domain;

import com.example.finalproject.global.domain.BaseTimeEntity;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.order.enums.OrderType;
import com.example.finalproject.order.enums.StoreOrderStatus;
import com.example.finalproject.store.domain.Store;
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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "store_orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreOrder extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_store_orders_order"))
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false, foreignKey = @ForeignKey(name = "fk_store_orders_store"))
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType = OrderType.REGULAR;

    @Column(name = "prep_time")
    private Integer prepTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StoreOrderStatus status = StoreOrderStatus.PENDING;

    @Column(name = "store_product_price", nullable = false)
    private Integer storeProductPrice;

    @Column(name = "delivery_fee", nullable = false)
    private Integer deliveryFee;

    @Column(name = "final_price", nullable = false)
    private Integer finalPrice;

    private LocalDateTime acceptedAt;
    private LocalDateTime preparedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Builder
    public StoreOrder(Order order, Store store, OrderType orderType,
            Integer storeProductPrice, Integer deliveryFee,
            Integer finalPrice) {
        this.order = order;
        this.store = store;
        this.orderType = orderType != null ? orderType : OrderType.REGULAR;
        this.storeProductPrice = storeProductPrice;
        this.deliveryFee = deliveryFee;
        this.finalPrice = finalPrice;
    }

    /**
     * 주문 접수 처리. 상태를 ACCEPTED로 변경하고 접수 시각을 기록한다 (배달 배차 가능).
     */
    public void accept() {
        this.status = StoreOrderStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
    }

    public void accept(Integer prepTime) {
        if (this.status != StoreOrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.STORE_ORDER_NOT_PENDING);
        }
        this.prepTime = prepTime;
        this.status = StoreOrderStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
    }

    public void reject(String reason) {
        if (this.status != StoreOrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.STORE_ORDER_NOT_PENDING);
        }
        this.status = StoreOrderStatus.REJECTED;
        this.cancelReason = reason;
        this.cancelledAt = LocalDateTime.now();
    }

    public void markReady() {
        if (this.status != StoreOrderStatus.ACCEPTED) {
            throw new BusinessException(ErrorCode.STORE_ORDER_NOT_ACCEPTED);
        }
        this.status = StoreOrderStatus.READY;
        this.preparedAt = LocalDateTime.now();
    }

    public void markPickedUp() {
        if (this.status != StoreOrderStatus.READY && this.status != StoreOrderStatus.ACCEPTED) {
            throw new BusinessException(ErrorCode.STORE_ORDER_NOT_READY);
        }
        this.status = StoreOrderStatus.PICKED_UP;
        this.pickedUpAt = LocalDateTime.now();
    }

    public void markDelivering() {
        if (this.status != StoreOrderStatus.PICKED_UP) {
            throw new BusinessException(ErrorCode.STORE_ORDER_NOT_PICKED_UP);
        }
        this.status = StoreOrderStatus.DELIVERING;
    }

    public void markDelivered() {
        if (this.status != StoreOrderStatus.DELIVERING) {
            throw new BusinessException(ErrorCode.STORE_ORDER_NOT_DELIVERING);
        }
        this.status = StoreOrderStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }
}
