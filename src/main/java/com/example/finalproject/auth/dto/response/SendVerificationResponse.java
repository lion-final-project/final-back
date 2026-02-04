package com.example.finalproject.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SendVerificationResponse {
    private String message;
    private int expiresIn;
    private int remainingAttempts;
}
