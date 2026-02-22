package com.example.finalproject.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostPaymentPrepareResponse {

    private Long orderId;
    private Long paymentId;
    private String pgOrderId;
    private Integer amount;
}
