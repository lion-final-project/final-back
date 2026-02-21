package com.example.finalproject.admin.dto.finance;

import com.example.finalproject.settlement.enums.SettlementStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminRiderSettlementListResponse {
    private List<Item> content;
    private Stats stats;
    private PageInfo page;

    @Getter
    @Builder
    public static class Item {
        private Long settlementId;
        private Long riderId;
        private String riderName;
        private String riderPhone;
        private String idCode;
        private String region;
        private long amount;
        private LocalDate settlementPeriodStart;
        private LocalDate settlementPeriodEnd;
        private LocalDateTime settledAt;
        private SettlementStatus status;
        private String statusLabel;
    }

    @Getter
    @Builder
    public static class Stats {
        private long total;
        private long completed;
        private long pending;
        private long failed;
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
