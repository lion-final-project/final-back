package com.example.finalproject.payment.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostPaymentConfirmResponse {

    private Long orderId;
    private Long paymentId;
    private String status;
    private LocalDateTime paidAt;
    private String receiptUrl;
}
