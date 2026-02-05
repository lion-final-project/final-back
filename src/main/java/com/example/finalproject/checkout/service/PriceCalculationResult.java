package com.example.finalproject.checkout.service;

import java.util.List;

public record PriceCalculationResult(
        PriceSummary priceSummary,
        List<StorePriceSummary> storeSummaries
) {

    public record PriceSummary(
            Integer productTotal,
            Integer deliveryTotal,
            Integer discount,
            Integer points,
            Integer finalTotal
    ) {
    }

    public record StorePriceSummary(
            Long storeId,
            Integer storeProductPrice,
            Integer deliveryFee,
            Integer storeFinalPrice
    ) {
    }
}
