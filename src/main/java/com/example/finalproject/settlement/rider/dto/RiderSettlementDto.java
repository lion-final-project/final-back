package com.example.finalproject.settlement.rider.dto;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 라이더 주간 정산 집계 DTO.
 * <p>
 * ItemReader → ItemProcessor 간 데이터 전달에 사용된다.
 * riderId 기준으로 한 주(月~日) 동안의 배달 수익 및 환불 집계를 담는다.
 * </p>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiderSettlementDto {

    /** 라이더 PK */
    private Long riderId;

    /** 정산 수령 은행명 (Rider.bankName 스냅샷) */
    private String bankName;

    /** 정산 계좌번호 (Rider.bankAccount 스냅샷) */
    private String bankAccount;

    /** 예금주 (Rider.accountHolder 스냅샷) */
    private String accountHolder;

    /** 기간 내 riderEarning 합계 */
    private int totalEarning;

    /** 기간 내 RIDER 귀책 환불 합계 */
    private int totalRefund;

    /** 정산 대상 기간 시작일 (직전 주 월요일) */
    private LocalDate periodStart;

    /** 정산 대상 기간 종료일 (직전 주 일요일) */
    private LocalDate periodEnd;

    /** 정산 대상 배달 ID 목록 */
    private List<Long> deliveryIds;
}