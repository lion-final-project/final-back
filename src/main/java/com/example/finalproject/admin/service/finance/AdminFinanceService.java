package com.example.finalproject.admin.service.finance;

import com.example.finalproject.admin.dto.finance.overview.AdminOverviewStatsResponse;
import com.example.finalproject.admin.dto.finance.payment.AdminPaymentListResponse;
import com.example.finalproject.admin.dto.finance.payment.AdminPaymentSummaryResponse;
import com.example.finalproject.admin.dto.finance.settlement.AdminRiderSettlementListResponse;
import com.example.finalproject.admin.dto.finance.settlement.AdminRiderSettlementSummaryResponse;
import com.example.finalproject.admin.dto.finance.settlement.AdminRiderSettlementTrendResponse;
import com.example.finalproject.admin.dto.finance.settlement.AdminStoreSettlementExecuteRequest;
import com.example.finalproject.admin.dto.finance.settlement.AdminStoreSettlementExecuteResponse;
import com.example.finalproject.admin.dto.finance.settlement.AdminStoreSettlementListResponse;
import com.example.finalproject.admin.dto.finance.settlement.AdminStoreSettlementSummaryResponse;
import com.example.finalproject.admin.dto.finance.settlement.AdminStoreSettlementTrendResponse;
import com.example.finalproject.admin.dto.finance.transaction.AdminTransactionDetailResponse;
import com.example.finalproject.admin.dto.finance.transaction.AdminTransactionOrderDetailResponse;
import com.example.finalproject.admin.dto.finance.transaction.AdminTransactionTrendResponse;
import com.example.finalproject.settlement.enums.SettlementStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminFinanceService {

    private final AdminFinanceOverviewService overviewService;
    private final AdminFinancePaymentService paymentService;
    private final AdminFinanceSettlementService settlementService;

    public AdminOverviewStatsResponse getOverviewStats(String adminEmail) {
        return overviewService.getOverviewStats(adminEmail);
    }

    public AdminTransactionTrendResponse getTransactionTrend(String adminEmail, String period) {
        return overviewService.getTransactionTrend(adminEmail, period);
    }

    public AdminTransactionDetailResponse getTransactionDetail(String adminEmail, String period, String label) {
        return overviewService.getTransactionDetail(adminEmail, period, label);
    }

    public AdminTransactionOrderDetailResponse getTransactionOrderDetail(String adminEmail, Long storeOrderId) {
        return overviewService.getTransactionOrderDetail(adminEmail, storeOrderId);
    }

    public AdminPaymentSummaryResponse getPaymentSummary(String adminEmail, String yearMonth) {
        return paymentService.getPaymentSummary(adminEmail, yearMonth);
    }

    public AdminPaymentListResponse getPayments(String adminEmail, String yearMonth, String keyword, Pageable pageable) {
        return paymentService.getPayments(adminEmail, yearMonth, keyword, pageable);
    }

    public AdminStoreSettlementSummaryResponse getStoreSettlementSummary(String adminEmail, String yearMonth) {
        return settlementService.getStoreSettlementSummary(adminEmail, yearMonth);
    }

    public AdminStoreSettlementTrendResponse getStoreSettlementTrend(String adminEmail, Integer months, String yearMonth) {
        return settlementService.getStoreSettlementTrend(adminEmail, months, yearMonth);
    }

    public AdminStoreSettlementListResponse getStoreSettlements(String adminEmail, String yearMonth, SettlementStatus status, String keyword, Pageable pageable) {
        return settlementService.getStoreSettlements(adminEmail, yearMonth, status, keyword, pageable);
    }

    public AdminRiderSettlementSummaryResponse getRiderSettlementSummary(String adminEmail, String yearMonth) {
        return settlementService.getRiderSettlementSummary(adminEmail, yearMonth);
    }

    public AdminRiderSettlementTrendResponse getRiderSettlementTrend(String adminEmail, Integer months, String yearMonth) {
        return settlementService.getRiderSettlementTrend(adminEmail, months, yearMonth);
    }

    public AdminRiderSettlementListResponse getRiderSettlements(String adminEmail, String yearMonth, SettlementStatus status, String keyword, Pageable pageable) {
        return settlementService.getRiderSettlements(adminEmail, yearMonth, status, keyword, pageable);
    }

    @Transactional
    public AdminStoreSettlementExecuteResponse executeStoreSettlement(String adminEmail, AdminStoreSettlementExecuteRequest request) {
        return settlementService.executeStoreSettlement(adminEmail, request);
    }

    @Transactional
    public AdminStoreSettlementExecuteResponse executeStoreSettlementSingle(String adminEmail, Long settlementId) {
        return settlementService.executeStoreSettlementSingle(adminEmail, settlementId);
    }

    @Transactional
    public AdminStoreSettlementExecuteResponse executeRiderSettlement(String adminEmail, AdminStoreSettlementExecuteRequest request) {
        return settlementService.executeRiderSettlement(adminEmail, request);
    }

    @Transactional
    public AdminStoreSettlementExecuteResponse executeRiderSettlementSingle(String adminEmail, Long settlementId) {
        return settlementService.executeRiderSettlementSingle(adminEmail, settlementId);
    }
}
