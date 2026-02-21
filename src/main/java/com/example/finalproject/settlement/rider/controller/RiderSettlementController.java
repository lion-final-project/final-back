package com.example.finalproject.settlement.rider.controller;

import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.settlement.rider.dto.response.GetRiderSettlementDetailResponse;
import com.example.finalproject.settlement.rider.dto.response.GetRiderSettlementListResponse;
import com.example.finalproject.settlement.rider.service.RiderSettlementQueryService;
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
@RequestMapping("/api/riders/settlements")
public class RiderSettlementController {

    private final RiderSettlementQueryService riderSettlementQueryService;

    @GetMapping
    public ResponseEntity<ApiResponse<GetRiderSettlementListResponse>> getSettlements(
            Authentication authentication,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        GetRiderSettlementListResponse response =
                riderSettlementQueryService.getSettlements(authentication.getName(), year, month);
        return ResponseEntity.ok(ApiResponse.success("정산 목록 조회가 완료되었습니다.", response));
    }

    @GetMapping("/{settlementId}")
    public ResponseEntity<ApiResponse<GetRiderSettlementDetailResponse>> getSettlementDetail(
            Authentication authentication,
            @PathVariable Long settlementId
    ) {
        GetRiderSettlementDetailResponse response =
                riderSettlementQueryService.getSettlementDetail(authentication.getName(), settlementId);
        return ResponseEntity.ok(ApiResponse.success("정산 상세 조회가 완료되었습니다.", response));
    }
}
