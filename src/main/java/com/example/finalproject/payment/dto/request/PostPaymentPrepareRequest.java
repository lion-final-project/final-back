package com.example.finalproject.payment.dto.request;

import com.example.finalproject.payment.enums.PaymentMethodType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostPaymentPrepareRequest {

    @NotEmpty
    private Map<Long, Integer> productQuantities;

    @NotNull
    private PaymentMethodType paymentMethod;

    @NotBlank
    private String deliveryAddress;

    private String deliveryRequest;
}

