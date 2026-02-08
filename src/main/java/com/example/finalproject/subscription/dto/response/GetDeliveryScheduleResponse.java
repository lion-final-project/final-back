package com.example.finalproject.subscription.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 마트 주간 배송 일정 응답 DTO.
 * 날짜별, 시간대별(3시간 단위) 배달 건수 및 품목별 준비 물량을 반환한다.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetDeliveryScheduleResponse {

    /** 조회 기준 주의 시작일 (월요일) */
    private LocalDate weekStartDate;

    /** 요일별 날짜 (월~일 순, 7개) */
    private List<LocalDate> weekDates;

    /** 날짜별 배송 정보 */
    private List<DateDeliveryInfo> dateDeliveries;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DateDeliveryInfo {
        private LocalDate date;
        private String dateLabel;  // 예: "2월 1일 (목)"
        private List<TimeSlotDeliveryInfo> timeSlots;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TimeSlotDeliveryInfo {
        /** 시간대 (예: "08:00~11:00") */
        private String timeSlot;
        /** 배달 건수 */
        private int deliveryCount;
        /** 품목별 준비 물량 */
        private List<ItemQuantity> items;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ItemQuantity {
        private String productName;
        private int quantity;
    }
}
