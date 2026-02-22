package com.example.finalproject.order.dto.storeorder.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetStoreSalesResponse {

    private int year;
    private int month;

    private long regularOrderCount;
    private long subscriptionOrderCount;

    private long totalSales;
    private double monthOverMonthRate;

    private long platformFee;
    private long pgFee;
    private long totalFee;

    private long refundAmount;
    private long refundCount;
    private long cancelledAmount;
    private long cancelledCount;

    private long totalOrderCount;
    private long averageOrderAmount;
    private long averageDailySales;
    private double dayOverDayRate;
    private long expectedSettlementAmount;

    private long todaySales;
    private long yesterdaySales;

    private SalesComposition salesComposition;
    private List<DailySalesPoint> dailySalesTrend;

    @Getter
    @Builder
    public static class SalesComposition {
        private long orderProductSales;
        private long subscriptionSales;
    }

    @Getter
    @Builder
    public static class DailySalesPoint {
        private String date;
        private long salesAmount;
        private long orderCount;
    }
}
