package com.example.finalproject.delivery.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PostRiderLocationRequest {
    private String riderId;
    private Double longitude; // 경도
    private Double latitude; // 위도
}
