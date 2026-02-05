package com.example.finalproject.product.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetProductStatsResponse {

    private Long totalProductCount;
    private Long activeProductCount;
    private Long inactiveProductCount;
    private Long todayInCount;
    private Long todayOutCount;

    public static GetProductStatsResponse of(Long totalProductCount, Long activeProductCount,
                                             Long inactiveProductCount, Long todayInCount, Long todayOutCount) {
        return GetProductStatsResponse.builder()
                .totalProductCount(totalProductCount)
                .activeProductCount(activeProductCount)
                .inactiveProductCount(inactiveProductCount)
                .todayInCount(todayInCount)
                .todayOutCount(todayOutCount)
                .build();
    }
}
