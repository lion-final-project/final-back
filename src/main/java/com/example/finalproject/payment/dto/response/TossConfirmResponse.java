package com.example.finalproject.payment.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TossConfirmResponse {

    private String paymentKey;
    private String orderId;
    private Integer totalAmount;

    private String status;
    private String approvedAt;
    private Receipt receipt;

    private Card card;

    @Getter
    @NoArgsConstructor
    public static class Receipt {
        private String url;
    }

    @Getter
    @NoArgsConstructor
    public static class Card {
        private String company;
        private String number;
    }
}
