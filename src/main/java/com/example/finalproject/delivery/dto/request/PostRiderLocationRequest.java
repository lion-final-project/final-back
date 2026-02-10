package com.example.finalproject.delivery.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PostRiderLocationRequest {
    private String riderId; // 레디스 라이더 식별자 (rider+_rider.id)
    private Double longitude; // 경도
    private Double latitude; // 위도
}
