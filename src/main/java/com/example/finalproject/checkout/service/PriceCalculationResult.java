package com.example.finalproject.checkout.service;

import java.util.List;

public record PriceCalculationResult(
        PriceSummary priceSummary,
        List<StorePriceSummary> storeSummaries
) {

    //총 가격 요약
    public record PriceSummary(
            Integer productTotal,
            Integer deliveryTotal,
            Integer discount,
            Integer points,
            Integer finalTotal
    ) {
    }

    //마트별 가격 요약
    public record StorePriceSummary(
            Long storeId,
            Integer storeProductPrice,
            Integer deliveryFee,
            Integer storeFinalPrice
    ) {
    }
}
