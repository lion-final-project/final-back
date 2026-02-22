package com.example.finalproject.payment.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TossBillingApproveResponse {
    private String paymentKey;
    private String orderId;
    private String orderName;
    private String status;
    private String approvedAt;

    private Card card;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Card {
        private String issuerCode;
        private String acquirerCode;
        private String number;
        private String cardType;
        private String ownerType;
    }
}
