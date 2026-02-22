package com.example.finalproject.admin.dto.finance.payment;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminPaymentListResponse {
    private List<Item> content;
    private Stats stats;
    private PageInfo page;

    @Getter
    @Builder
    public static class Item {
        private Long storeOrderId;
        private String orderNumber;
        private String mart;
        private String category;
        private String region;
        private String customerName;
        private long amount;
        private long commission;
        private long refundAmount;
        private String status;
        private String paymentStatus;
        private String paymentMethod;
        private LocalDateTime paidAt;
        private LocalDateTime orderedAt;
    }

    @Getter
    @Builder
    public static class Stats {
        private long totalAmount;
        private long totalCommission;
        private long totalRefundAmount;
        private long netRevenue;
    }

    @Getter
    @Builder
    public static class PageInfo {
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean hasNext;
    }
}
