package com.example.finalproject.admin.dto.finance.settlement;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminStoreSettlementExecuteRequest {

    @NotBlank
    private String yearMonth;
}
