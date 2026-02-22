package com.example.finalproject.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VerifyPhoneResponse {
    private boolean verified = true;
    private String phoneVerificationToken;
}
