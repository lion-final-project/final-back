package com.example.finalproject.admin.dto.finance;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminOverviewStatsResponse {
    private long totalUsers;
    private long approvedStores;
    private long deliveringRiders;
    private long pendingStoreSettlements;
    private long pendingReports;
    private long pendingInquiries;
}
