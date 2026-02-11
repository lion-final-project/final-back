package com.example.finalproject.delivery.domain;


import com.example.finalproject.delivery.enums.DeliveryStatus;
import com.example.finalproject.global.domain.BaseTimeEntity;
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

@Entity
@Table(name = "deliveries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_order_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_deliveries_store_order"))
    private StoreOrder storeOrder;

    @Column(columnDefinition = "GEOGRAPHY(POINT,4326)")
    private Point storeLocation;

    @Column(columnDefinition = "GEOGRAPHY(POINT,4326)")
    private Point customerLocation;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id",
            foreignKey = @ForeignKey(name = "fk_deliveries_rider"))
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
     * 라이더가 배달을 수락합니다.
     */
    public void accept(Rider rider) {
        this.rider = rider;
        this.status = DeliveryStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
    }

    /**
     * 라이더가 상품을 픽업합니다.
     */
    public void pickUp() {
        this.status = DeliveryStatus.PICKED_UP;
        this.pickedUpAt = LocalDateTime.now();
    }

    /**
     * 배달이 완료됩니다.
     */
    public void complete() {
        this.status = DeliveryStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }

    /**
     * 배달을 취소합니다.
     */
    public void cancel(String reason) {
        this.status = DeliveryStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancelReason = reason;
    }
}