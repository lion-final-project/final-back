package com.example.finalproject.payment.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostBillingKeyIssueResponse {

    private String cardCompany;
    private String cardNumberMasked;
}
