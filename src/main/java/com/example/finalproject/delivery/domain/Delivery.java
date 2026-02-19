package com.example.finalproject.delivery.domain;

import com.example.finalproject.delivery.enums.DeliveryStatus;
import com.example.finalproject.global.domain.BaseTimeEntity;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.order.domain.StoreOrder;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.*;
import org.locationtech.jts.geom.Point;

/**
 * 배달 엔티티.
 * <p>
 * 매장주문(StoreOrder)에 대한 배달을 나타냅니다.
 * 상태 전이: REQUESTED → ACCEPTED → PICKED_UP → DELIVERING → DELIVERED (또는
 * CANCELLED)
 * 각 상태 전이는 도메인 메서드에서 검증됩니다.
 * </p>
 */
@Entity
@Table(name = "deliveries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_order_id", nullable = false, unique = true, foreignKey = @ForeignKey(name = "fk_deliveries_store_order"))
    private StoreOrder storeOrder;

    @Column(columnDefinition = "GEOGRAPHY(POINT,4326)")
    private Point storeLocation;

    @Column(columnDefinition = "GEOGRAPHY(POINT,4326)")
    private Point customerLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id", foreignKey = @ForeignKey(name = "fk_deliveries_rider"))
    private Rider rider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status = DeliveryStatus.REQUESTED;

    @Column(name = "delivery_fee", nullable = false)
    private Integer deliveryFee;

    @Column(name = "rider_earning")
    private Integer riderEarning;

    @Column(name = "distance_km", precision = 5, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes;

    private LocalDateTime acceptedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;



    @Builder
    public Delivery(StoreOrder storeOrder,
            Point storeLocation,
            Point customerLocation,
            Integer deliveryFee,
            BigDecimal distanceKm,
            Integer estimatedMinutes) {
        this.storeOrder = storeOrder;
        this.storeLocation = storeLocation;
        this.customerLocation = customerLocation;
        this.deliveryFee = deliveryFee;
        this.distanceKm = distanceKm;
        this.estimatedMinutes = estimatedMinutes;
    }

    /**
     * REQUESTED → ACCEPTED
     */
    public void accept(Rider rider) {
        validateStatusTransition(DeliveryStatus.REQUESTED);
        this.rider = rider;
        this.status = DeliveryStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
    }

    /*
     * ACCEPTED → PICKED_UP
     */
    public void pickUp() {
        validateStatusTransition(DeliveryStatus.ACCEPTED);
        this.status = DeliveryStatus.PICKED_UP;
        this.pickedUpAt = LocalDateTime.now();
    }

    /**
     * PICKED_UP → DELIVERING
     */
    public void startDelivering() {
        validateStatusTransition(DeliveryStatus.PICKED_UP);
        this.status = DeliveryStatus.DELIVERING;
    }

    /**
     * DELIVERING → DELIVERED
     */
    public void complete() {
        validateStatusTransition(DeliveryStatus.DELIVERING);
        this.status = DeliveryStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }

    /**
     * REQUESTED | ACCEPTED | PICKED_UP | DELIVERING → CANCELLED
     */
    public void cancel(String reason) {
        if (this.status == DeliveryStatus.DELIVERED || this.status == DeliveryStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.DELIVERY_INVALID_STATUS_TRANSITION);
        }
        this.status = DeliveryStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancelReason = reason;
    }

    /**
     * 현재 상태가 expectedStatus인지 검증
     */
    private void validateStatusTransition(DeliveryStatus expectedCurrentStatus) {
        if (this.status != expectedCurrentStatus) {
            throw new BusinessException(ErrorCode.DELIVERY_INVALID_STATUS_TRANSITION);
        }
    }
}