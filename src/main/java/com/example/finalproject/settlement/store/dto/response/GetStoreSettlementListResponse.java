package com.example.finalproject.settlement.store.dto.response;

import com.example.finalproject.settlement.enums.SettlementStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetStoreSettlementListResponse {
    private List<Item> content;

    @Getter
    @Builder
    public static class Item {
        private Long settlementId;
        private int year;
        private int month;
        private LocalDate settlementPeriodStart;
        private LocalDate settlementPeriodEnd;
        private long totalSales;
        private long platformFee;
        private long pgFee;
        private long settlementAmount;
        private SettlementStatus status;
        private LocalDateTime settledAt;
    }
}
