package com.example.finalproject.settlement.rider.dto.response;

import com.example.finalproject.settlement.domain.Settlement;
import com.example.finalproject.settlement.enums.SettlementStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetRiderSettlementListResponse {

    private List<Item> content;

    @Getter
    @Builder
    public static class Item {
        private Long settlementId;
        private LocalDate settlementPeriodStart;
        private LocalDate settlementPeriodEnd;
        private long totalEarning;
        private long refundAdjustment;
        private long settlementAmount;
        private SettlementStatus status;
        private String bankName;
        private String bankAccount;
        private LocalDateTime settledAt;

        public static Item from(Settlement s) {
            return Item.builder()
                    .settlementId(s.getId())
                    .settlementPeriodStart(s.getSettlementPeriodStart())
                    .settlementPeriodEnd(s.getSettlementPeriodEnd())
                    .totalEarning(s.getTotalSales())
                    .refundAdjustment(s.getRefundAdjustment())
                    .settlementAmount(s.getSettlementAmount())
                    .status(s.getStatus())
                    .bankName(s.getBankName())
                    .bankAccount(s.getBankAccount())
                    .settledAt(s.getSettledAt())
                    .build();
        }
    }
}
