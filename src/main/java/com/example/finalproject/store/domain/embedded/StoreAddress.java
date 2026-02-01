package com.example.finalproject.store.domain.embedded;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoreAddress {

    @Column(name = "address_line1", nullable = false, length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(columnDefinition = "GEOGRAPHY(POINT,4326)")
    private Point location;

    @Builder
    public StoreAddress(String addressLine1, String addressLine2, Point location) {
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.location = location;
    }
}
