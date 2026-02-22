package com.example.finalproject.payment.util;

import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.order.domain.StoreOrder;
import com.example.finalproject.order.enums.StoreOrderStatus;
import com.example.finalproject.payment.enums.RefundResponsibility;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
public class RefundAmountCalculator {

    /**
     * 책임 주체 + 주문 상태 기준으로 환불 금액을 산정
     * <p>
     * 정책(대화 기반): - 환불 요청은 배송 완료(DELIVERED) 후 48시간 이내에만 가능 - CUSTOMER(단순 변심, 고객 귀책): 배달비 제외(상품가만 환불) -
     * STORE/PLATFORM/RIDER: 전액 환불(상품가+배달비)로 처리 (현 단계 단순화)
     */
    public int calculate(StoreOrder storeOrder, RefundResponsibility responsibility) {

        if (storeOrder.getStatus() != StoreOrderStatus.REFUND_REQUESTED) {
            throw new BusinessException(ErrorCode.REFUND_REQUEST_NOT_ALLOWED);
        }

        if (storeOrder.getDeliveredAt() == null) {
            throw new BusinessException(ErrorCode.INVALID_STORE_ORDER_STATUS);
        }
        if (storeOrder.getDeliveredAt().plusHours(48).isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.REFUND_EXPIRED);
        }

        int storeProductPrice = safeAmount(storeOrder.getStoreProductPrice());
        int deliveryFee = safeAmount(storeOrder.getDeliveryFee());
        int finalPrice = safeAmount(storeOrder.getFinalPrice());

        if (finalPrice != storeProductPrice + deliveryFee) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_AMOUNT);
        }

        if (responsibility == RefundResponsibility.CUSTOMER) {
            if (storeProductPrice <= 0) {
                throw new BusinessException(ErrorCode.INVALID_REFUND_AMOUNT);
            }
            return storeProductPrice;
        }

        if (finalPrice <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_AMOUNT);
        }
        return finalPrice;
    }

    private int safeAmount(Integer value) {
        return value == null ? 0 : value;
    }
}