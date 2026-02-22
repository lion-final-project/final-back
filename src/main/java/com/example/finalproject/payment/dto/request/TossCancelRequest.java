package com.example.finalproject.payment.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TossCancelRequest {
    private String cancelReason;
    private Integer cancelAmount;
}

