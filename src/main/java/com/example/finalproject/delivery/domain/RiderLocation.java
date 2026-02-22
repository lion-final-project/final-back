package com.example.finalproject.delivery.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "rider_locations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RiderLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rider_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_rider_locations_rider"))
    private Rider rider;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id",
            foreignKey = @ForeignKey(name = "fk_rider_locations_delivery"))
    private Delivery delivery;

    @Column(nullable = false, columnDefinition = "GEOGRAPHY(POINT,4326)")
    private Point location;

    @Column(precision = 6, scale = 2)
    private BigDecimal accuracy;

    @Column(precision = 5, scale = 2)
    private BigDecimal speed;

    @Column(precision = 5, scale = 2)
    private BigDecimal heading;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "is_current", nullable = false)
    private Boolean isCurrent = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public RiderLocation(Rider rider, Delivery delivery, Point location,
                         LocalDateTime recordedAt) {
        this.rider = rider;
        this.delivery = delivery;
        this.location = location;
        this.recordedAt = recordedAt;
    }
}