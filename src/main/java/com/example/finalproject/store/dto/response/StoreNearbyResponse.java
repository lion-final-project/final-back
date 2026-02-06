package com.example.finalproject.store.dto.response;

import com.example.finalproject.global.util.GeometryUtil;
import com.example.finalproject.store.domain.Store;
import com.example.finalproject.store.enums.StoreActiveStatus;
import com.querydsl.core.annotations.QueryProjection;
import lombok.*;

@Getter
@NoArgsConstructor
@ToString
public class StoreNearbyResponse {
    private Long storeId;
    private String storeName;
    private Double distance;
    private Integer reviewCount;
    private String storeImage;
    private Boolean isOpen;
    private String addressLine1;
    private String addressLine2;
    private Double latitude;
    private Double longitude;

    @Builder
    @QueryProjection
    public StoreNearbyResponse(Long storeId, String storeName, Double distance, Integer reviewCount, String storeImage,
            Boolean isOpen, String addressLine1, String addressLine2, Double latitude, Double longitude) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.distance = distance;
        this.reviewCount = reviewCount;
        this.storeImage = storeImage;
        this.isOpen = isOpen;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
