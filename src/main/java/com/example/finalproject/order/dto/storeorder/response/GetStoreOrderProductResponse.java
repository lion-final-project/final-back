package com.example.finalproject.order.dto.storeorder.response;

import com.example.finalproject.order.domain.OrderProduct;
import lombok.Builder;
import lombok.Getter;



/**
 * 마트 주문 상세 응답 DTO
 */
@Getter
@Builder
public class GetStoreOrderProductResponse {

    private Long productId;
    private String productName;
    private Integer price;
    private Integer productQuantity;

    public static GetStoreOrderProductResponse from(OrderProduct orderProducts) {

        return GetStoreOrderProductResponse.builder()
                .productId(orderProducts.getProduct().getId())
                .productName(orderProducts.getProductNameSnapshot())
                .price(orderProducts.getPriceSnapshot())
                .productQuantity(orderProducts.getQuantity())
                .build();
    }
}
