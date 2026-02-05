package com.example.finalproject.product.dto.response;

import com.example.finalproject.product.domain.StockEventType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StockAdjustResponse {

    private Long productId;
    private String productName;
    private StockEventType eventType;
    private Integer quantity;
    private Integer stockAfter;

    public static StockAdjustResponse of(Long productId, String productName,
                                         StockEventType eventType, Integer quantity, Integer stockAfter) {
        return StockAdjustResponse.builder()
                .productId(productId)
                .productName(productName)
                .eventType(eventType)
                .quantity(quantity)
                .stockAfter(stockAfter)
                .build();
    }
}
