package com.example.finalproject.admin.dto.finance;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminTransactionOrderDetailResponse {
    private Long storeOrderId;
    private String orderNumber;
    private String storeName;
    private String customerName;
    private String riderName;
    private String riderPhone;
    private Integer deliveryFee;
    private String deliveryAddress;
    private String deliveryLocation;
    private LocalDateTime orderedAt;
    private String paymentStatus;
    private String refundStatus;
    private long refundAmount;
    private List<ProductItem> products;

    @Getter
    @Builder
    public static class ProductItem {
        private String name;
        private Integer unitPrice;
        private Integer quantity;
    }
}

