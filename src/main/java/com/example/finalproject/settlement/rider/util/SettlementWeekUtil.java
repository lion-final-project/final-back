package com.example.finalproject.settlement.rider.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 라이더 주간 정산 날짜 범위 계산 유틸.
 * <p>
 * 기준일(today) 기준으로 직전 주(月~日)의 시작/종료 시각을 반환한다.
 * 예) 목요일(2026-02-19) 실행 시 → 2026-02-09(월) 00:00:00 ~ 2026-02-15(일) 23:59:59
 * </p>
 */
public final class SettlementWeekUtil {

    private SettlementWeekUtil() {}

    /**
     * 직전 주 월요일 00:00:00 (LocalDateTime)
     */
    public static LocalDateTime prevWeekStart(LocalDate today) {
        return prevWeekStartDate(today).atStartOfDay();
    }

    /**
     * 직전 주 일요일 23:59:59.999999999 (LocalDateTime)
     */
    public static LocalDateTime prevWeekEnd(LocalDate today) {
        return prevWeekEndDate(today).atTime(LocalTime.MAX);
    }

    /**
     * 직전 주 월요일 (LocalDate)
     */
    public static LocalDate prevWeekStartDate(LocalDate today) {
        return today.with(DayOfWeek.MONDAY).minusWeeks(1);
    }

    /**
     * 직전 주 일요일 (LocalDate)
     */
    public static LocalDate prevWeekEndDate(LocalDate today) {
        return prevWeekStartDate(today).with(DayOfWeek.SUNDAY);
    }
}
