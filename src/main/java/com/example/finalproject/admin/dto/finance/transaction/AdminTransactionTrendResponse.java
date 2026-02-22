package com.example.finalproject.admin.dto.finance.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminTransactionTrendResponse {
    private String period;
    @JsonProperty("xLabels")
    private List<String> xLabels;
    @JsonProperty("yValues")
    private List<Long> yValues;
    private long maxY;
}
