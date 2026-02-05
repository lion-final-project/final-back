package com.example.finalproject.product.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostProductResponse {

    private Long productId;
    private String productName;
    private Integer price;
    private Integer stock;

    public static PostProductResponse of(Long productId, String productName, Integer price, Integer stock) {
        return PostProductResponse.builder()
                .productId(productId)
                .productName(productName)
                .price(price)
                .stock(stock)
                .build();
    }
}
