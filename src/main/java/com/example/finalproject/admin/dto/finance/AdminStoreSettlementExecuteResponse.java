package com.example.finalproject.admin.dto.finance;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminStoreSettlementExecuteResponse {
    private String yearMonth;
    private int completedCount;
}
