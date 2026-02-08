package com.example.finalproject.subscription.dto.response;

import com.example.finalproject.subscription.enums.SubscriptionStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 고객 구독 조회 응답 DTO (UC-C10 구독관리).
 * API-SUB-002 목록 조회 등에 사용.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetSubscriptionResponse {

    private Long subscriptionId;
    private Long storeId;
    private String storeName;
    private Long subscriptionProductId;
    private String subscriptionProductName;
    private SubscriptionStatus status;
    private Integer totalAmount;
    private String deliveryTimeSlot;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate nextPaymentDate;

    /** 월 기준 총 배송 횟수 (구독 상품 설정) */
    private Integer totalDeliveryCount;
    /** 완료된 배송 건수 (현재 주기 기준) */
    private Integer completedDeliveryCount;

    /** 구독 구성 품목 (상품명, 수량) */
    private List<SubscriptionItemDto> items;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime pausedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime cancelledAt;

    private String cancelReason;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SubscriptionItemDto {
        private String productName;
        private Integer quantity;
    }
}
