package com.example.finalproject.settlement.store.controller;

import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.settlement.store.dto.response.GetStoreSettlementDetailResponse;
import com.example.finalproject.settlement.store.dto.response.GetStoreSettlementListResponse;
import com.example.finalproject.settlement.store.service.StoreSettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/store/settlements")
public class StoreSettlementController {

    private final StoreSettlementService storeSettlementService;

    @GetMapping
    public ResponseEntity<ApiResponse<GetStoreSettlementListResponse>> getSettlements(
            Authentication authentication,
            @RequestParam(required = false) Integer year
    ) {
        // 사장님 계정 기준 연도별 정산 목록 조회
        GetStoreSettlementListResponse response =
                storeSettlementService.getSettlements(authentication.getName(), year);
        return ResponseEntity.ok(ApiResponse.success("정산 목록 조회가 완료되었습니다.", response));
    }

    @GetMapping("/{settlementId}")
    public ResponseEntity<ApiResponse<GetStoreSettlementDetailResponse>> getSettlementDetail(
            Authentication authentication,
            @PathVariable Long settlementId
    ) {
        // 선택한 정산 건의 상세(포함 주문 목록 포함) 조회
        GetStoreSettlementDetailResponse response =
                storeSettlementService.getSettlementDetail(authentication.getName(), settlementId);
        return ResponseEntity.ok(ApiResponse.success("정산 상세 조회가 완료되었습니다.", response));
    }
}
