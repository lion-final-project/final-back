package com.example.finalproject.settlement.store.dto.response;

import com.example.finalproject.settlement.store.enums.SettlementStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetStoreSettlementDetailResponse {
    private Long settlementId;
    private long storeId;
    private String storeName;
    private int year;
    private int month;
    private LocalDate settlementPeriodStart;
    private LocalDate settlementPeriodEnd;
    private long totalSales;
    private long platformFee;
    private long pgFee;
    private long totalFee;
    private long settlementAmount;
    private SettlementStatus status;
    private String bankName;
    private String bankAccount;
    private LocalDateTime settledAt;
    private List<OrderItem> orders;

    @Getter
    @Builder
    public static class OrderItem {
        private Long storeOrderId;
        private Long orderId;
        private String orderNumber;
        private LocalDateTime deliveredAt;
        private long amount;
        private long fee;
        private long netAmount;
    }
}
