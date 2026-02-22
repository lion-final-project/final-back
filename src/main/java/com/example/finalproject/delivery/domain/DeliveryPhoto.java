package com.example.finalproject.delivery.domain;


import com.example.finalproject.global.domain.BaseTimeEntity;
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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "delivery_photos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryPhoto extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_delivery_photos_delivery"))
    private Delivery delivery;

    @Column(name = "photo_url", nullable = false, length = 500)
    private String photoUrl;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime deletedAt;

    @Builder
    public DeliveryPhoto(Delivery delivery, String photoUrl, LocalDateTime expiresAt) {
        this.delivery = delivery;
        this.photoUrl = photoUrl;
        this.expiresAt = expiresAt;
    }
}