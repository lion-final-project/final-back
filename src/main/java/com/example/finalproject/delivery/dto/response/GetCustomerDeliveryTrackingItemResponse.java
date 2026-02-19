package com.example.finalproject.delivery.dto.response;

import com.example.finalproject.delivery.enums.DeliveryStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetCustomerDeliveryTrackingItemResponse {
    private Long deliveryId;
    private Long orderId;
    private Long storeOrderId;
    private String orderNumber;
    private String storeName;
    private DeliveryStatus deliveryStatus;
    private String trackingStep;
    private String trackingStepLabel;
    private Integer estimatedMinutes;
    private LocalDateTime estimatedArrivalAt;
    private LocalDateTime updatedAt;
}
