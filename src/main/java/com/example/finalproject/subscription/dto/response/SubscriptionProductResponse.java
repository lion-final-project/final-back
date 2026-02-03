package com.example.finalproject.subscription.dto.response;

import com.example.finalproject.subscription.enums.SubscriptionProductStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 구독 상품 응답 DTO.
 * API-SOP-010 (등록 201), API-SOP-009 (마트 목록), API-STO-005 (고객용 마트별 조회) 응답에 공통 사용.
 * null 필드는 JSON 응답에서 제외된다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionProductResponse {

    private Long subscriptionProductId;
    private String name;
    private String description;
    private Integer price;
    private Integer totalDeliveryCount;
    private SubscriptionProductStatus status;
    private Integer subscriberCount;
    private String imageUrl;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private List<SubscriptionProductItemResponse> items;

    /**
     * 구독 상품 구성 품목 응답 DTO.
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SubscriptionProductItemResponse {

        private Long productId;
        private String productName;
        private Integer quantity;
    }
}
