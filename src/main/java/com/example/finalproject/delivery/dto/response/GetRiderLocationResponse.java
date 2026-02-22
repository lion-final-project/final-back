package com.example.finalproject.delivery.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetRiderLocationResponse {
    private String riderId;
    private Double longitude;
    private Double latitude;
}