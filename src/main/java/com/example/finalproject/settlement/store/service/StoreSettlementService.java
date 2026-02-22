package com.example.finalproject.settlement.store.service;

import com.example.finalproject.settlement.store.dto.response.GetStoreSettlementDetailResponse;
import com.example.finalproject.settlement.store.dto.response.GetStoreSettlementListResponse;
import java.time.YearMonth;

/**
 * 마트 정산 서비스.
 * - 사장님 화면 조회(UC-S08)
 * - 월 배치 정산 생성/완료(UC-S06, UC-S08 백엔드 기반)
 */
public interface StoreSettlementService {
    /**
     * 사장님 계정 기준 월별 정산 목록 조회.
     *
     * @param ownerEmail 마트 사장님 이메일
     * @param year 조회 연도(미입력 시 현재 연도)
     */
    GetStoreSettlementListResponse getSettlements(String ownerEmail, Integer year);

    /**
     * 사장님 계정 기준 정산 상세 조회.
     * 포함 주문 라인(정산 상세)까지 반환한다.
     *
     * @param ownerEmail 마트 사장님 이메일
     * @param settlementId 정산 ID
     */
    GetStoreSettlementDetailResponse getSettlementDetail(String ownerEmail, Long settlementId);

    /**
     * 대상 월 정산 원장(마스터 + 상세) 생성.
     */
    void generateMonthlySettlements(YearMonth targetMonth);

    /**
     * 생성된 PENDING 정산을 완료 처리.
     *
     * @return 완료 처리된 건수
     */
    int completePendingSettlements(YearMonth targetMonth);
}
