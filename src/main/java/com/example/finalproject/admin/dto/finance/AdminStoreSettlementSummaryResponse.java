package com.example.finalproject.admin.dto.finance;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminStoreSettlementSummaryResponse {
    private long totalTargets;
    private long completedTargets;
    private long pendingTargets;
    private long failedTargets;
    private long totalSettlementAmount;
    private double completedRate;
}
