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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_deliveries_rider"))
    private Rider rider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "delivery_status DEFAULT 'REQUESTED'")
    private DeliveryStatus status = DeliveryStatus.REQUESTED;

    @Column(name = "delivery_fee", nullable = false)
    private Integer deliveryFee;

    @Column(name = "rider_earning", nullable = false)
    private Integer riderEarning;

    @Column(name = "distance_km", precision = 5, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    private LocalDateTime acceptedAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Builder
    public Delivery(StoreOrder storeOrder, Rider rider,
                    Integer deliveryFee, Integer riderEarning,
                    LocalDateTime requestedAt) {
        this.storeOrder = storeOrder;
        this.rider = rider;
        this.deliveryFee = deliveryFee;
        this.riderEarning = riderEarning;
        this.requestedAt = requestedAt;
    }
}