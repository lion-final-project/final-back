package com.example.finalproject.delivery.dto.response;

import com.example.finalproject.delivery.domain.Delivery;
import com.example.finalproject.delivery.enums.DeliveryStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 배달 목록 응답 DTO.
 * <p>
 * 배달 ID, 매장명, 상태, 배달비, 시간 정보를 포함합니다.
 * </p>
 */
@Getter
@Builder
public class GetDeliveryResponse {

    private Long id;

    @JsonProperty("store-order-id")
    private Long storeOrderId;

    @JsonProperty("store-name")
    private String storeName;

    private DeliveryStatus status;

    @JsonProperty("delivery-fee")
    private Integer deliveryFee;

    @JsonProperty("created-at")
    private LocalDateTime createdAt;

    @JsonProperty("accepted-at")
    private LocalDateTime acceptedAt;

    @JsonProperty("delivered-at")
    private LocalDateTime deliveredAt;

    public static GetDeliveryResponse from(Delivery delivery) {
        return GetDeliveryResponse.builder()
                .id(delivery.getId())
                .storeOrderId(delivery.getStoreOrder().getId())
                .storeName(delivery.getStoreOrder().getStore().getStoreName())
                .status(delivery.getStatus())
                .deliveryFee(delivery.getDeliveryFee())
                .createdAt(delivery.getCreatedAt())
                .acceptedAt(delivery.getAcceptedAt())
                .deliveredAt(delivery.getDeliveredAt())
                .build();
    }
}
