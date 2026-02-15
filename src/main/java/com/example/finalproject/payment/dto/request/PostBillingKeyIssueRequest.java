package com.example.finalproject.payment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostBillingKeyIssueRequest {

    @NotBlank
    private String authKey;

    @NotBlank
    private String customerKey;
}
