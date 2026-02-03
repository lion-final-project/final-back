package com.example.finalproject.subscription.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 구독 상품 등록 요청 DTO.
 * API-SOP-010 (구독 상품 등록) Request Body에 대응한다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostSubscriptionProductRequest {

    @NotBlank(message = "구독 상품명은 필수입니다.")
    @Size(max = 200)
    private String name;

    private String description;

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    private Integer price;

    @NotNull(message = "총 배송 횟수는 필수입니다.")
    @Min(value = 1, message = "총 배송 횟수는 1 이상이어야 합니다.")
    private Integer totalDeliveryCount;

    @NotEmpty(message = "구성 품목은 1개 이상이어야 합니다.")
    @Valid
    private List<PostSubscriptionProductItemRequest> items;

    /**
     * 구독 상품 구성 품목 요청 DTO.
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PostSubscriptionProductItemRequest {

        @NotNull(message = "상품 ID는 필수입니다.")
        private Long productId;

        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
        private Integer quantity;
    }
}
