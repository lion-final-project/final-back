package com.example.finalproject.order.domain;

import static com.example.finalproject.order.enums.StoreOrderStatus.CANCELLED;
import static com.example.finalproject.order.enums.StoreOrderStatus.PENDING;
import static com.example.finalproject.order.enums.StoreOrderStatus.REJECTED;

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
    private StoreOrderStatus status = PENDING;

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
        if (this.status != PENDING) {
            throw new BusinessException(ErrorCode.STORE_ORDER_NOT_PENDING);
        }
        this.prepTime = prepTime;
        this.status = StoreOrderStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
    }

    /**
     * PENDING 주문을 즉시 거절 (PG 환불 없이 사용하는 경우용).
     * 환불이 필요한 거절은 {@link #requestReject()} 후 PG 취소를 호출하고, 환불 완료 시 {@link #completeReject(String)}를 사용한다.
     */
    public void reject(String reason) {
        if (this.status != PENDING) {
            throw new BusinessException(ErrorCode.STORE_ORDER_NOT_PENDING);
        }
        this.status = REJECTED;
        this.cancelReason = reason;
        this.cancelledAt = LocalDateTime.now();
    }

    /**
     * 마트 거절 요청. PG 호출 전에 상태를 REJECT_REQUESTED로 두어, 환불 완료 이벤트에서 completeReject로 구분할 수 있게 한다.
     */
    public void requestReject() {
        if (this.status != PENDING) {
            throw new BusinessException(ErrorCode.STORE_ORDER_NOT_PENDING);
        }
        this.status = StoreOrderStatus.REJECT_REQUESTED;
    }

    /**
     * 환불 완료 후 거절 확정. REJECT_REQUESTED → REJECTED, 사유·시각 저장.
     */
    public void completeReject(String reason) {
        if (this.status != StoreOrderStatus.REJECT_REQUESTED) {
            throw new BusinessException(ErrorCode.INVALID_STORE_ORDER_REFUND_STATUS);
        }
        this.status = REJECTED;
        this.cancelReason = reason;
        this.cancelledAt = LocalDateTime.now();
    }

    public void cancel(String reason) {
        if (this.status != StoreOrderStatus.CANCEL_REQUESTED) {
            throw new BusinessException(ErrorCode.ORDER_CANNOT_BE_CANCELLED);
        }
        this.status = CANCELLED;
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


    public boolean isCancelledOrRejected() {
        return status == StoreOrderStatus.CANCELLED
                || status == StoreOrderStatus.REJECTED;
    }

    public void requestCancel() {
        switch (this.status) {
            case PENDING -> this.status = StoreOrderStatus.CANCEL_REQUESTED;

            case CANCEL_REQUESTED -> throw new BusinessException(ErrorCode.ALREADY_PROCESSED_PAYMENT);

            case ACCEPTED, READY, PICKED_UP, DELIVERING, DELIVERED, CANCELLED, REJECTED, REJECT_REQUESTED ->
                    throw new BusinessException(ErrorCode.ORDER_CANNOT_BE_CANCELLED);
        }
    }

    public boolean isCancelRequested() {
        return this.status == StoreOrderStatus.CANCEL_REQUESTED;
    }

    public boolean isRejectRequested() {
        return this.status == StoreOrderStatus.REJECT_REQUESTED;
    }
}
