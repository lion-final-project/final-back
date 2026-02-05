package com.example.finalproject.product.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StockEventType {

    IN("입고"),
    OUT("출고");

    private final String description;
}
