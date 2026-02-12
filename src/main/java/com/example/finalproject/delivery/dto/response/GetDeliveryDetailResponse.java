package com.example.finalproject.delivery.dto.response;

import com.example.finalproject.delivery.domain.Delivery;
import com.example.finalproject.delivery.enums.DeliveryStatus;
import com.example.finalproject.global.util.GeometryUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 배달 상세 응답 DTO.
 * <p>
 * 목록 응답보다 상세한 정보를 포함합니다:
 * 매장/고객 좌표(PostGIS), 거리, 예상 시간, 취소 사유 등.
 * PostGIS Point 객체는 GeometryUtil로 경도/위도로 변환하여 응답합니다.
 * </p>
 */
@Getter
@Builder
public class GetDeliveryDetailResponse {

    private Long id;

    @JsonProperty("store-order-id")
    private Long storeOrderId;

    @JsonProperty("store-name")
    private String storeName;

    private DeliveryStatus status;

    @JsonProperty("delivery-fee")
    private Integer deliveryFee;

    @JsonProperty("rider-earning")
    private Integer riderEarning;

    @JsonProperty("distance-km")
    private BigDecimal distanceKm;

    @JsonProperty("estimated-minutes")
    private Integer estimatedMinutes;

    @JsonProperty("store-longitude")
    private Double storeLongitude;

    @JsonProperty("store-latitude")
    private Double storeLatitude;

    @JsonProperty("customer-longitude")
    private Double customerLongitude;

    @JsonProperty("customer-latitude")
    private Double customerLatitude;

    @JsonProperty("cancel-reason")
    private String cancelReason;

    @JsonProperty("created-at")
    private LocalDateTime createdAt;

    @JsonProperty("accepted-at")
    private LocalDateTime acceptedAt;

    @JsonProperty("picked-up-at")
    private LocalDateTime pickedUpAt;

    @JsonProperty("delivered-at")
    private LocalDateTime deliveredAt;

    @JsonProperty("cancelled-at")
    private LocalDateTime cancelledAt;

    public static GetDeliveryDetailResponse from(Delivery delivery) {
        GetDeliveryDetailResponseBuilder builder = GetDeliveryDetailResponse.builder()
                .id(delivery.getId())
                .storeOrderId(delivery.getStoreOrder().getId())
                .storeName(delivery.getStoreOrder().getStore().getStoreName())
                .status(delivery.getStatus())
                .deliveryFee(delivery.getDeliveryFee())
                .riderEarning(delivery.getRiderEarning())
                .distanceKm(delivery.getDistanceKm())
                .estimatedMinutes(delivery.getEstimatedMinutes())
                .cancelReason(delivery.getCancelReason())
                .createdAt(delivery.getCreatedAt())
                .acceptedAt(delivery.getAcceptedAt())
                .pickedUpAt(delivery.getPickedUpAt())
                .deliveredAt(delivery.getDeliveredAt())
                .cancelledAt(delivery.getCancelledAt());

        if (delivery.getStoreLocation() != null) {
            builder.storeLongitude(GeometryUtil.getLongitude(delivery.getStoreLocation()))
                    .storeLatitude(GeometryUtil.getLatitude(delivery.getStoreLocation()));
        }
        if (delivery.getCustomerLocation() != null) {
            builder.customerLongitude(GeometryUtil.getLongitude(delivery.getCustomerLocation()))
                    .customerLatitude(GeometryUtil.getLatitude(delivery.getCustomerLocation()));
        }

        return builder.build();
    }
}
