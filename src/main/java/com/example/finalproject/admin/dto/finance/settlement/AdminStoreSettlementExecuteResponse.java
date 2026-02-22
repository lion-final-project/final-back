package com.example.finalproject.admin.dto.finance.settlement;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminStoreSettlementExecuteResponse {
    private String yearMonth;
    private int completedCount;
}
