package com.example.finalproject.product.dto.response;

import com.example.finalproject.product.domain.ProductStockHistory;
import com.example.finalproject.product.domain.StockEventType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GetStockHistoryResponse {

    private Long historyId;
    private Long productId;
    private String productName;
    private StockEventType eventType;
    private Integer quantity;
    private Integer stockAfter;
    private LocalDateTime createdAt;

    public static GetStockHistoryResponse from(ProductStockHistory history) {
        return GetStockHistoryResponse.builder()
                .historyId(history.getId())
                .productId(history.getProduct().getId())
                .productName(history.getProduct().getProductName())
                .eventType(history.getEventType())
                .quantity(history.getQuantity())
                .stockAfter(history.getStockAfter())
                .createdAt(history.getCreatedAt())
                .build();
    }
}
