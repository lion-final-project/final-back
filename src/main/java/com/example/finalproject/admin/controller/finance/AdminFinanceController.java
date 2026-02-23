package com.example.finalproject.admin.controller.finance;

import com.example.finalproject.admin.dto.finance.AdminOverviewStatsResponse;
import com.example.finalproject.admin.dto.finance.AdminPaymentListResponse;
import com.example.finalproject.admin.dto.finance.AdminPaymentSummaryResponse;
import com.example.finalproject.admin.dto.finance.AdminRiderSettlementListResponse;
import com.example.finalproject.admin.dto.finance.AdminRiderSettlementSummaryResponse;
import com.example.finalproject.admin.dto.finance.AdminRiderSettlementTrendResponse;
import com.example.finalproject.admin.dto.finance.AdminStoreSettlementExecuteRequest;
import com.example.finalproject.admin.dto.finance.AdminStoreSettlementExecuteResponse;
import com.example.finalproject.admin.dto.finance.AdminStoreSettlementListResponse;
import com.example.finalproject.admin.dto.finance.AdminStoreSettlementSummaryResponse;
import com.example.finalproject.admin.dto.finance.AdminStoreSettlementTrendResponse;
import com.example.finalproject.admin.dto.finance.AdminTransactionDetailResponse;
import com.example.finalproject.admin.dto.finance.AdminTransactionOrderDetailResponse;
import com.example.finalproject.admin.dto.finance.AdminTransactionTrendResponse;
import com.example.finalproject.admin.service.finance.AdminFinanceService;
import com.example.finalproject.global.response.ApiResponse;
import com.example.finalproject.settlement.enums.SettlementStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/finance")
public class AdminFinanceController {

    private final AdminFinanceService adminFinanceService;

    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<AdminOverviewStatsResponse>> getOverview(Authentication authentication) {
        AdminOverviewStatsResponse response = adminFinanceService.getOverviewStats(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success("관리자 전체 현황 통계 조회가 완료되었습니다.", response));
    }

    @GetMapping("/overview/transactions")
    public ResponseEntity<ApiResponse<AdminTransactionTrendResponse>> getTransactionTrend(
            Authentication authentication,
            @RequestParam(required = false, defaultValue = "weekly") String period
    ) {
        AdminTransactionTrendResponse response = adminFinanceService.getTransactionTrend(authentication.getName(), period);
        return ResponseEntity.ok(ApiResponse.success("거래액 추이 조회가 완료되었습니다.", response));
    }

    @GetMapping("/overview/transactions/details")
    public ResponseEntity<ApiResponse<AdminTransactionDetailResponse>> getTransactionDetails(
            Authentication authentication,
            @RequestParam(required = false, defaultValue = "weekly") String period,
            @RequestParam String label
    ) {
        AdminTransactionDetailResponse response = adminFinanceService.getTransactionDetail(authentication.getName(), period, label);
        return ResponseEntity.ok(ApiResponse.success("거래액 상세 주문 조회가 완료되었습니다.", response));
    }

    @GetMapping("/overview/transactions/orders/{storeOrderId}")
    public ResponseEntity<ApiResponse<AdminTransactionOrderDetailResponse>> getTransactionOrderDetail(
            Authentication authentication,
            @PathVariable Long storeOrderId
    ) {
        AdminTransactionOrderDetailResponse response =
                adminFinanceService.getTransactionOrderDetail(authentication.getName(), storeOrderId);
        return ResponseEntity.ok(ApiResponse.success("주문 상세 정보 조회가 완료되었습니다.", response));
    }

    @GetMapping("/payments/summary")
    public ResponseEntity<ApiResponse<AdminPaymentSummaryResponse>> getPaymentSummary(
            Authentication authentication,
            @RequestParam(required = false) String yearMonth
    ) {
        AdminPaymentSummaryResponse response = adminFinanceService.getPaymentSummary(authentication.getName(), yearMonth);
        return ResponseEntity.ok(ApiResponse.success("결제 요약 통계 조회가 완료되었습니다.", response));
    }

    @GetMapping("/payments")
    public ResponseEntity<ApiResponse<AdminPaymentListResponse>> getPayments(
            Authentication authentication,
            @RequestParam(required = false) String yearMonth,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        AdminPaymentListResponse response = adminFinanceService.getPayments(
                authentication.getName(),
                yearMonth,
                keyword,
                pageable
        );
        return ResponseEntity.ok(ApiResponse.success("결제 목록 조회가 완료되었습니다.", response));
    }

    @GetMapping("/settlements/store/summary")
    public ResponseEntity<ApiResponse<AdminStoreSettlementSummaryResponse>> getStoreSettlementSummary(
            Authentication authentication,
            @RequestParam(required = false) String yearMonth
    ) {
        AdminStoreSettlementSummaryResponse response =
                adminFinanceService.getStoreSettlementSummary(authentication.getName(), yearMonth);
        return ResponseEntity.ok(ApiResponse.success("마트 정산 요약 통계 조회가 완료되었습니다.", response));
    }

    @GetMapping("/settlements/store/trend")
    public ResponseEntity<ApiResponse<AdminStoreSettlementTrendResponse>> getStoreSettlementTrend(
            Authentication authentication,
            @RequestParam(required = false, defaultValue = "6") Integer months,
            @RequestParam(required = false) String yearMonth
    ) {
        AdminStoreSettlementTrendResponse response =
                adminFinanceService.getStoreSettlementTrend(authentication.getName(), months, yearMonth);
        return ResponseEntity.ok(ApiResponse.success("마트 정산 추이 조회가 완료되었습니다.", response));
    }

    @GetMapping("/settlements/store")
    public ResponseEntity<ApiResponse<AdminStoreSettlementListResponse>> getStoreSettlements(
            Authentication authentication,
            @RequestParam(required = false) String yearMonth,
            @RequestParam(required = false) SettlementStatus status,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "settlementPeriodStart", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        AdminStoreSettlementListResponse response = adminFinanceService.getStoreSettlements(
                authentication.getName(),
                yearMonth,
                status,
                keyword,
                pageable
        );
        return ResponseEntity.ok(ApiResponse.success("마트 정산 목록 조회가 완료되었습니다.", response));
    }

    @PostMapping("/settlements/store/execute")
    public ResponseEntity<ApiResponse<AdminStoreSettlementExecuteResponse>> executeStoreSettlement(
            Authentication authentication,
            @Valid @RequestBody AdminStoreSettlementExecuteRequest request
    ) {
        AdminStoreSettlementExecuteResponse response =
                adminFinanceService.executeStoreSettlement(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("마트 일괄 정산이 완료되었습니다.", response));
    }

    @PostMapping("/settlements/store/{settlementId}/execute")
    public ResponseEntity<ApiResponse<AdminStoreSettlementExecuteResponse>> executeStoreSettlementSingle(
            Authentication authentication,
            @PathVariable Long settlementId
    ) {
        AdminStoreSettlementExecuteResponse response =
                adminFinanceService.executeStoreSettlementSingle(authentication.getName(), settlementId);
        return ResponseEntity.ok(ApiResponse.success("마트 개별 정산이 완료되었습니다.", response));
    }

    @GetMapping("/settlements/rider/summary")
    public ResponseEntity<ApiResponse<AdminRiderSettlementSummaryResponse>> getRiderSettlementSummary(
            Authentication authentication,
            @RequestParam(required = false) String yearMonth
    ) {
        AdminRiderSettlementSummaryResponse response =
                adminFinanceService.getRiderSettlementSummary(authentication.getName(), yearMonth);
        return ResponseEntity.ok(ApiResponse.success("라이더 정산 요약 통계 조회가 완료되었습니다.", response));
    }

    @GetMapping("/settlements/rider/trend")
    public ResponseEntity<ApiResponse<AdminRiderSettlementTrendResponse>> getRiderSettlementTrend(
            Authentication authentication,
            @RequestParam(required = false, defaultValue = "6") Integer months,
            @RequestParam(required = false) String yearMonth
    ) {
        AdminRiderSettlementTrendResponse response =
                adminFinanceService.getRiderSettlementTrend(authentication.getName(), months, yearMonth);
        return ResponseEntity.ok(ApiResponse.success("라이더 정산 추이 조회가 완료되었습니다.", response));
    }

    @GetMapping("/settlements/rider")
    public ResponseEntity<ApiResponse<AdminRiderSettlementListResponse>> getRiderSettlements(
            Authentication authentication,
            @RequestParam(required = false) String yearMonth,
            @RequestParam(required = false) SettlementStatus status,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "settlementPeriodStart", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        AdminRiderSettlementListResponse response = adminFinanceService.getRiderSettlements(
                authentication.getName(),
                yearMonth,
                status,
                keyword,
                pageable
        );
        return ResponseEntity.ok(ApiResponse.success("라이더 정산 목록 조회가 완료되었습니다.", response));
    }

    @PostMapping("/settlements/rider/execute")
    public ResponseEntity<ApiResponse<AdminStoreSettlementExecuteResponse>> executeRiderSettlement(
            Authentication authentication,
            @Valid @RequestBody AdminStoreSettlementExecuteRequest request
    ) {
        AdminStoreSettlementExecuteResponse response =
                adminFinanceService.executeRiderSettlement(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("라이더 일괄 정산이 완료되었습니다.", response));
    }

    @PostMapping("/settlements/rider/{settlementId}/execute")
    public ResponseEntity<ApiResponse<AdminStoreSettlementExecuteResponse>> executeRiderSettlementSingle(
            Authentication authentication,
            @PathVariable Long settlementId
    ) {
        AdminStoreSettlementExecuteResponse response =
                adminFinanceService.executeRiderSettlementSingle(authentication.getName(), settlementId);
        return ResponseEntity.ok(ApiResponse.success("라이더 개별 정산이 완료되었습니다.", response));
    }
}