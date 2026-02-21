package com.example.finalproject.admin.dto.finance;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminStoreSettlementTrendResponse {
    @JsonProperty("xLabels")
    private List<String> xLabels;
    @JsonProperty("yValues")
    private List<Long> yValues;
    private long totalAmount;
    private double changeRate;
}
