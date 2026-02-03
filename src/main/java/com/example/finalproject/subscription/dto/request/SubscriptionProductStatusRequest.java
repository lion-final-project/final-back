package com.example.finalproject.subscription.dto.request;

import com.example.finalproject.subscription.enums.SubscriptionProductStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 구독 상품 노출 상태 변경 요청 DTO.
 * API-SOP-010S (노출 상태 변경) Request Body에 대응한다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionProductStatusRequest {

    @NotNull(message = "노출 상태는 필수입니다.")
    private SubscriptionProductStatus status;
}
