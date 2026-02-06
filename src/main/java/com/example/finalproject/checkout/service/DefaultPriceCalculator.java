package com.example.finalproject.checkout.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class DefaultPriceCalculator implements PriceCalculator {

    @Override
    public PriceCalculationResult calculate(List<CheckoutItem> items, DeliveryPolicy deliveryPolicy, int discount, int points) {
        Map<Long, Integer> storeProductTotal = new LinkedHashMap<>();
        for (CheckoutItem item : items) {
            storeProductTotal.merge(item.storeId(), item.unitPrice() * item.quantity(), Integer::sum);
        }

        // 매장별 상품 가격 계산
        List<PriceCalculationResult.StorePriceSummary> summaries = storeProductTotal.entrySet().stream()
                .map(entry -> {
                    int fee = deliveryPolicy.deliveryFee(entry.getKey());
                    return new PriceCalculationResult.StorePriceSummary(
                            entry.getKey(),
                            entry.getValue(),
                            fee,
                            entry.getValue() + fee
                    );
                })
                .toList();

        int productTotal = summaries.stream().mapToInt(PriceCalculationResult.StorePriceSummary::storeProductPrice).sum();
        int deliveryTotal = summaries.stream().mapToInt(PriceCalculationResult.StorePriceSummary::deliveryFee).sum();
        int finalTotal = productTotal + deliveryTotal - (discount + points);

        return new PriceCalculationResult(
                new PriceCalculationResult.PriceSummary(productTotal, deliveryTotal, discount, points, finalTotal),
                summaries
        );
    }
}
