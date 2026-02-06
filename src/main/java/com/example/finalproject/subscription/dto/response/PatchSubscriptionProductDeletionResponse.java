package com.example.finalproject.subscription.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 구독 상품 삭제 요청 처리 결과.
 * 삭제 요청은 구독자 존재 여부에 따라 즉시 삭제되거나 삭제 예정 상태로 전환된다.
 */
@Getter
@Builder
public class PatchSubscriptionProductDeletionResponse {

    private final Action action;
    private final GetSubscriptionProductResponse product;

    /**
     * 삭제 요청 처리 결과 유형.
     */
    public enum Action {
        SCHEDULED,
        DELETED
    }

    public static PatchSubscriptionProductDeletionResponse scheduled(GetSubscriptionProductResponse product) {
        return PatchSubscriptionProductDeletionResponse.builder()
                .action(Action.SCHEDULED)
                .product(product)
                .build();
    }

    public static PatchSubscriptionProductDeletionResponse deleted() {
        return PatchSubscriptionProductDeletionResponse.builder()
                .action(Action.DELETED)
                .product(null)
                .build();
    }
}
