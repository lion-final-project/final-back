package com.example.finalproject.checkout.service;

import java.util.List;

public interface PriceCalculator {

    PriceCalculationResult calculate(List<CheckoutItem> items, DeliveryPolicy deliveryPolicy, int discount, int points);

    record CheckoutItem(Long productId, Long storeId, Integer unitPrice, Integer quantity) {
    }

    //배달비 정책
    @FunctionalInterface
    interface DeliveryPolicy {
        int deliveryFee(Long storeId);
    }
}
