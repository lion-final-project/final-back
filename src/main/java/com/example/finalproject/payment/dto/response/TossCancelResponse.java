package com.example.finalproject.payment.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TossCancelResponse {
    private String paymentKey;
    private String status;
}

