package com.example.finalproject.admin.dto.finance.transaction;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminTransactionDetailResponse {
    private String period;
    private String label;
    private LocalDateTime rangeStart;
    private LocalDateTime rangeEnd;
    private long totalCount;
    private long totalAmount;
    private List<Item> content;

    @Getter
    @Builder
    public static class Item {
        private Long storeOrderId;
        private String orderNumber;
        private String storeName;
        private String customerName;
        private long amount;
        private String orderStatus;
        private LocalDateTime orderedAt;
    }
}
