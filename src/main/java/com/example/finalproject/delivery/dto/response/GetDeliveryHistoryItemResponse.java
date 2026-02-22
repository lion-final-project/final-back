package com.example.finalproject.delivery.dto.response;

import com.example.finalproject.delivery.domain.Delivery;
import com.example.finalproject.delivery.enums.DeliveryStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

/**
 * 배달 이력 목록 응답 DTO.
 * <p>
 * 고객의 완료(DELIVERED) 및 취소(CANCELLED) 배달 이력을 반환합니다.
 * </p>
 */
@Getter
@Builder
public class GetDeliveryHistoryItemResponse {

    private Long deliveryId;
    private Long orderId;
    private Long storeOrderId;
    private String orderNumber;
    private String storeName;
    private String deliveryAddress;
    private DeliveryStatus deliveryStatus;
    private Integer deliveryFee;
    private LocalDateTime deliveredAt;
    private LocalDateTime cancelledAt;
    private String cancelReason;
    private LocalDateTime createdAt;
    private boolean isSettled;

    public static GetDeliveryHistoryItemResponse from(Delivery delivery) {
        return GetDeliveryHistoryItemResponse.builder()
                .deliveryId(delivery.getId())
                .orderId(delivery.getStoreOrder().getOrder().getId())
                .storeOrderId(delivery.getStoreOrder().getId())
                .orderNumber(delivery.getStoreOrder().getOrder().getOrderNumber())
                .storeName(delivery.getStoreOrder().getStore().getStoreName())
                .deliveryAddress(delivery.getStoreOrder().getOrder().getDeliveryAddress())
                .deliveryStatus(delivery.getStatus())
                .deliveryFee(delivery.getDeliveryFee())
                .deliveredAt(delivery.getDeliveredAt())
                .cancelledAt(delivery.getCancelledAt())
                .cancelReason(delivery.getCancelReason())
                .createdAt(delivery.getCreatedAt())
                .isSettled(delivery.isSettled())
                .build();
    }
}
