package com.example.finalproject.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostPaymentConfirmRequest {

    @NotNull
    private Long paymentId;

    @NotBlank
    private String paymentKey;
}
