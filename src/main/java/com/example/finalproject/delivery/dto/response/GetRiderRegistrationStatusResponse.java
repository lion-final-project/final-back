package com.example.finalproject.delivery.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GetRiderRegistrationStatusResponse {
    private String status;
    private Long approvalId;
}
