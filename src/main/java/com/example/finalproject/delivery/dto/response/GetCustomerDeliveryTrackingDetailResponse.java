package com.example.finalproject.delivery.dto.response;

import com.example.finalproject.delivery.enums.DeliveryStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetCustomerDeliveryTrackingDetailResponse {
    private Long deliveryId;
    private Long orderId;
    private Long storeOrderId;
    private String orderNumber;
    private String storeName;
    private String deliveryAddress;
    private DeliveryStatus deliveryStatus;
    private String trackingStep;
    private String trackingStepLabel;
    private Integer estimatedMinutes;
    private LocalDateTime estimatedArrivalAt;
    private LocalDateTime orderReceivedAt;
    private LocalDateTime preparingAt;
    private LocalDateTime pickupWaitingAt;
    private LocalDateTime deliveringAt;
    private LocalDateTime deliveredAt;
    private Long riderId;
    private String riderName;
    private String riderPhone;
    private RiderLocation riderLocation;
    private List<String> deliveryPhotoUrls;

    @Getter
    @Builder
    public static class RiderLocation {
        private Double longitude;
        private Double latitude;
    }
}
