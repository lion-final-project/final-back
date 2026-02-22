package com.example.finalproject.store.domain;

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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "store_business_hours",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_business_hours_store_day",
                columnNames = {"store_id", "day_of_week"}
        ))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreBusinessHour extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_business_hours_store"))
    private Store store;

    @Column(name = "day_of_week", nullable = false)
    private Short dayOfWeek;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    @Column(name = "is_closed", nullable = false)
    private Boolean isClosed = false;

    @Builder
    public StoreBusinessHour(Store store, Short dayOfWeek,
                             LocalTime openTime, LocalTime closeTime, Boolean isClosed) {
        this.store = store;
        this.dayOfWeek = dayOfWeek;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isClosed = isClosed != null ? isClosed : false;
    }

    public void assignStore(Store store) {
        this.store = store;
    }


    public void update(LocalTime openTime, LocalTime closeTime, Boolean isClosed) {
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isClosed = isClosed != null ? isClosed : false;
    }
}
