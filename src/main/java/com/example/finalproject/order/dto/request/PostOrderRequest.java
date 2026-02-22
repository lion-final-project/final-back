package com.example.finalproject.order.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 주문 생성 요청 DTO (API-ORD-001).
 * 시영님 결제/재검증 로직과 인터페이스 맞춤.
 * 검증: 내 장바구니 소유, 수량>=1, 주소·결제수단 존재 및 본인 소유.
 */
@Getter
@Setter
@NoArgsConstructor
public class PostOrderRequest {

    @NotNull(message = "배송지 ID는 필수입니다.")
    private Long addressId;

    @NotNull(message = "결제수단 ID는 필수입니다.")
    private Long paymentMethodId;

    ///일반결제는 미전송(null) → 주문 즉시 배달 구독결제에서만 사용
    @Pattern(regexp = "08:00~11:00|11:00~14:00|14:00~17:00|17:00~20:00",
            message = "배달 시간대는 08:00~11:00, 11:00~14:00, 14:00~17:00, 17:00~20:00 중 하나여야 합니다.")
    private String deliveryTimeSlot;

    @Size(max = 255)
    private String deliveryRequest;

    @NotEmpty(message = "주문할 장바구니 상품이 없습니다.")
    private List<Long> cartItemIds;

    // 쿠폰 ID 미사용 시 null
    private Long couponId;

    //사용 포인트 미사용 시 null 또는 0
    private Integer usePoints;

    public String getDeliveryRequestOrEmpty() {
        return deliveryRequest != null ? deliveryRequest : "";
    }

    //사용 포인트 null 또는 음수면 0으로 간주
    public int getUsePointsOrZero() {
        return usePoints != null && usePoints >= 0 ? usePoints : 0;
    }
}
