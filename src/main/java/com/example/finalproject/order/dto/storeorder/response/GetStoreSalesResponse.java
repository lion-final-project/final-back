package com.example.finalproject.order.dto.storeorder.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetStoreSalesResponse {

    private int year;
    private int month;

    // 주문 유형별 건수
    private long regularOrderCount;
    private long subscriptionOrderCount;

    // 매출 요약
    private long totalSales;
    private double monthOverMonthRate;

    // 수수료
    private long platformFee;

    // 환불
    private long refundAmount;
    private long refundCount;

    // 통계
    private long totalOrderCount;
    private long averageOrderAmount;
    private double dayOverDayRate;

    /** 오늘 총 매출 (요청한 연월이 이번 달일 때만 의미 있음, 대시보드용) */
    private long todaySales;
    /** 어제 총 매출 (요청한 연월이 이번 달일 때만 의미 있음) */
    private long yesterdaySales;
}
