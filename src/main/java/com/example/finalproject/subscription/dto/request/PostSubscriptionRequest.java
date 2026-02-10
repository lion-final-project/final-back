package com.example.finalproject.subscription.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 구독 신청 요청 DTO (API-SUB-001).
 * deliveryDays: 0=일, 1=월, 2=화, 3=수, 4=목, 5=금, 6=토
 * deliveryTimeSlot: 08:00~11:00, 11:00~14:00, 14:00~17:00, 17:00~20:00
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostSubscriptionRequest {

    @NotNull(message = "구독 상품 ID는 필수입니다.")
    private Long subscriptionProductId;

    @NotNull(message = "배송지 ID는 필수입니다.")
    private Long addressId;

    /** 결제 수단 ID. null이면 해당 사용자의 기본 결제 수단 사용 */
    private Long paymentMethodId;

    /** 배송 요일 (0=일, 1=월, …, 6=토). 비어있으면 구독 상품의 배송 요일 사용 */
    private List<Integer> deliveryDays;

    /** 희망 배송 시간대. 08:00~11:00, 11:00~14:00, 14:00~17:00, 17:00~20:00 중 하나 */
    private String deliveryTimeSlot;
}
