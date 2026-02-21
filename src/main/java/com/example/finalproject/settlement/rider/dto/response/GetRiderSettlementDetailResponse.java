package com.example.finalproject.settlement.rider.dto.response;

import com.example.finalproject.settlement.domain.RiderSettlementDetail;
import com.example.finalproject.settlement.domain.Settlement;
import com.example.finalproject.settlement.enums.SettlementStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetRiderSettlementDetailResponse {

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
    private List<DeliveryItem> deliveries;

    @Getter
    @Builder
    public static class DeliveryItem {
        private Long deliveryId;
        private LocalDateTime deliveredAt;
        private int riderEarning;
        private int refundAmount;
        private int netAmount;
        private BigDecimal distanceKm;

        public static DeliveryItem from(RiderSettlementDetail detail) {
            return DeliveryItem.builder()
                    .deliveryId(detail.getDelivery().getId())
                    .deliveredAt(detail.getDelivery().getDeliveredAt())
                    .riderEarning(detail.getRiderEarning())
                    .refundAmount(detail.getRefundAmount())
                    .netAmount(detail.getNetAmount())
                    .distanceKm(detail.getDelivery().getDistanceKm())
                    .build();
        }
    }

    public static GetRiderSettlementDetailResponse of(Settlement s, List<RiderSettlementDetail> details) {
        return GetRiderSettlementDetailResponse.builder()
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
                .deliveries(details.stream().map(DeliveryItem::from).toList())
                .build();
    }
}
