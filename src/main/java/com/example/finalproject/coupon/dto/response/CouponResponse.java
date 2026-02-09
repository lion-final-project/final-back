package com.example.finalproject.coupon.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CouponResponse {

    private Long id;
    private String name;
    private Integer discountAmount;
}
