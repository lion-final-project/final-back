package com.example.finalproject.payment.service.pg;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CancelResult {
    private final int cumulativeCanceledAmount;
}

