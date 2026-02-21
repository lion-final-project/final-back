package com.example.finalproject.settlement.store.service;

import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.order.domain.StoreOrder;
import com.example.finalproject.order.enums.StoreOrderStatus;
import com.example.finalproject.order.repository.StoreOrderRepository;
import com.example.finalproject.payment.enums.RefundResponsibility;
import com.example.finalproject.payment.enums.RefundStatus;
import com.example.finalproject.payment.repository.PaymentRefundRepository;
import com.example.finalproject.settlement.domain.Settlement;
import com.example.finalproject.settlement.domain.SettlementDetail;
import com.example.finalproject.settlement.store.dto.response.GetStoreSettlementDetailResponse;
import com.example.finalproject.settlement.store.dto.response.GetStoreSettlementListResponse;
import com.example.finalproject.settlement.enums.SettlementStatus;
import com.example.finalproject.settlement.enums.SettlementTargetType;
import com.example.finalproject.settlement.store.repository.SettlementDetailRepository;
import com.example.finalproject.settlement.store.repository.SettlementRepository;
import com.example.finalproject.store.domain.Store;
import com.example.finalproject.store.domain.embedded.SettlementAccount;
import com.example.finalproject.store.enums.StoreStatus;
import com.example.finalproject.store.repository.StoreRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreSettlementServiceImpl implements StoreSettlementService {

    private static final double PLATFORM_FEE_RATE = 0.05d;
    private static final double PG_FEE_RATE = 0.033d;

    private final StoreRepository storeRepository;
    private final StoreOrderRepository storeOrderRepository;
    private final SettlementRepository settlementRepository;
    private final SettlementDetailRepository settlementDetailRepository;
    private final PaymentRefundRepository paymentRefundRepository;

    @Override
    public GetStoreSettlementListResponse getSettlements(String ownerEmail, Integer year) {
        Store store = storeRepository.findByOwnerEmail(ownerEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        int targetYear = year != null ? year : LocalDate.now().getYear();
        LocalDate start = LocalDate.of(targetYear, 1, 1);
        LocalDate end = LocalDate.of(targetYear, 12, 31);

        List<GetStoreSettlementListResponse.Item> content = settlementRepository
                .findByTargetTypeAndTargetIdAndSettlementPeriodStartBetweenOrderBySettlementPeriodStartDesc(
                        SettlementTargetType.STORE, store.getId(), start, end)
                .stream()
                .map(s -> GetStoreSettlementListResponse.Item.builder()
                        .settlementId(s.getId())
                        .year(s.getSettlementPeriodStart().getYear())
                        .month(s.getSettlementPeriodStart().getMonthValue())
                        .settlementPeriodStart(s.getSettlementPeriodStart())
                        .settlementPeriodEnd(s.getSettlementPeriodEnd())
                        .totalSales(s.getTotalSales())
                        .platformFee(s.getPlatformFee())
                        .pgFee(s.getPgFee())
                        .settlementAmount(s.getSettlementAmount())
                        .status(s.getStatus())
                        .settledAt(s.getSettledAt())
                        .build())
                .toList();

        return GetStoreSettlementListResponse.builder()
                .content(content)
                .build();
    }

    @Override
    public GetStoreSettlementDetailResponse getSettlementDetail(String ownerEmail, Long settlementId) {
        Store store = storeRepository.findByOwnerEmail(ownerEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "정산 내역을 찾을 수 없습니다."));

        if (settlement.getTargetType() != SettlementTargetType.STORE
                || !settlement.getTargetId().equals(store.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "해당 정산 내역에 접근할 수 없습니다.");
        }

        List<GetStoreSettlementDetailResponse.OrderItem> orders = settlementDetailRepository
                .findAllBySettlementIdWithStoreOrder(settlementId)
                .stream()
                .map(sd -> GetStoreSettlementDetailResponse.OrderItem.builder()
                        .storeOrderId(sd.getStoreOrder().getId())
                        .orderId(sd.getStoreOrder().getOrder().getId())
                        .orderNumber(sd.getStoreOrder().getOrder().getOrderNumber())
                        .deliveredAt(sd.getStoreOrder().getDeliveredAt())
                        .amount(sd.getAmount())
                        .fee(sd.getFee())
                        .netAmount(sd.getNetAmount())
                        .build())
                .toList();

        return GetStoreSettlementDetailResponse.builder()
                .settlementId(settlement.getId())
                .storeId(store.getId())
                .storeName(store.getStoreName())
                .year(settlement.getSettlementPeriodStart().getYear())
                .month(settlement.getSettlementPeriodStart().getMonthValue())
                .settlementPeriodStart(settlement.getSettlementPeriodStart())
                .settlementPeriodEnd(settlement.getSettlementPeriodEnd())
                .totalSales(settlement.getTotalSales())
                .platformFee(settlement.getPlatformFee())
                .pgFee(settlement.getPgFee())
                .totalFee(settlement.getPlatformFee() + settlement.getPgFee())
                .settlementAmount(settlement.getSettlementAmount())
                .status(settlement.getStatus())
                .bankName(settlement.getBankName())
                .bankAccount(settlement.getBankAccount())
                .settledAt(settlement.getSettledAt())
                .orders(orders)
                .build();
    }

    @Override
    @Transactional
    public void generateMonthlySettlements(YearMonth targetMonth) {
        LocalDate periodStart = targetMonth.atDay(1);
        LocalDate periodEnd = targetMonth.atEndOfMonth();
        LocalDateTime startAt = periodStart.atStartOfDay();
        LocalDateTime endAt = periodEnd.atTime(LocalTime.MAX);

        List<Long> storeIds = storeOrderRepository.findDistinctStoreIdsByStatusAndDeliveredAtBetween(
                StoreOrderStatus.DELIVERED, startAt, endAt);

        for (Long storeId : storeIds) {
            processStoreSettlement(storeId, targetMonth, periodStart, periodEnd, startAt, endAt);
        }
    }

    @Override
    @Transactional
    public int completePendingSettlements(YearMonth targetMonth) {
        LocalDate start = targetMonth.atDay(1);
        LocalDate end = targetMonth.atEndOfMonth();

        List<Settlement> pendings = settlementRepository
                .findByTargetTypeAndStatusAndSettlementPeriodStartAndSettlementPeriodEnd(
                        SettlementTargetType.STORE,
                        SettlementStatus.PENDING,
                        start,
                        end
                );

        for (Settlement pending : pendings) {
            pending.complete(LocalDateTime.now());
        }

        return pendings.size();
    }

    private void processStoreSettlement(
            Long storeId,
            YearMonth targetMonth,
            LocalDate periodStart,
            LocalDate periodEnd,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        Store store = storeRepository.findById(storeId).orElse(null);
        if (store == null || store.getStatus() != StoreStatus.APPROVED) {
            return;
        }

        List<StoreOrder> deliveredOrders = storeOrderRepository.findByStoreIdAndStatusAndDeliveredAtBetween(
                storeId,
                StoreOrderStatus.DELIVERED,
                startAt,
                endAt
        );
        if (deliveredOrders.isEmpty()) {
            return;
        }

        Settlement settlement = settlementRepository
                .findByTargetTypeAndTargetIdAndSettlementPeriodStartAndSettlementPeriodEnd(
                        SettlementTargetType.STORE,
                        storeId,
                        periodStart,
                        periodEnd
                )
                .orElseGet(() -> createEmptySettlement(store, storeId, periodStart, periodEnd));

        if (!settlement.isFailed() && settlement.getStatus() == SettlementStatus.COMPLETED) {
            return;
        }

        try {
            settlementDetailRepository.deleteBySettlementId(settlement.getId());

            int totalSales = 0;
            int platformFee = 0;
            int pgFee = 0;
            int settlementAmount = 0;

            for (StoreOrder storeOrder : deliveredOrders) {
                int gross = storeOrder.getFinalPrice();
                int platform = (int) Math.round(gross * PLATFORM_FEE_RATE);
                int pg = (int) Math.round(gross * PG_FEE_RATE);
                int fee = platform + pg;
                int net = Math.max(0, gross - fee);

                totalSales += gross;
                platformFee += platform;
                pgFee += pg;
                settlementAmount += net;

                settlementDetailRepository.save(SettlementDetail.builder()
                        .settlement(settlement)
                        .storeOrder(storeOrder)
                        .amount(gross)
                        .fee(fee)
                        .netAmount(net)
                        .build());
            }

            LocalDateTime cutoffStartExclusive = targetMonth.atDay(20).atStartOfDay();
            LocalDateTime cutoffEndInclusive = targetMonth.plusMonths(1).atDay(20).atStartOfDay();

            int refundAdjustment = (int) paymentRefundRepository.sumApprovedStoreRefundAmountByStoreIdAndCutoffBetween(
                    storeId,
                    cutoffStartExclusive,
                    cutoffEndInclusive,
                    RefundStatus.APPROVED,
                    RefundResponsibility.STORE
            );

            int finalSettlementAmount = Math.max(0, settlementAmount - refundAdjustment);
            settlement.updateSummary(totalSales, platformFee, pgFee, refundAdjustment, finalSettlementAmount);
        } catch (Exception exception) {
            String message = exception.getMessage();
            if (message == null || message.isBlank()) {
                message = "정산 처리 중 알 수 없는 오류";
            }
            if (message.length() > 500) {
                message = message.substring(0, 500);
            }
            settlement.fail(message);
            log.error("store settlement processing failed. storeId={}, period={}-{}",
                    storeId, periodStart, periodEnd, exception);
        }
    }

    private Settlement createEmptySettlement(Store store, Long storeId, LocalDate periodStart, LocalDate periodEnd) {
        SettlementAccount account = store.getSettlementAccount();
        return settlementRepository.save(Settlement.builder()
                .targetType(SettlementTargetType.STORE)
                .targetId(storeId)
                .settlementPeriodStart(periodStart)
                .settlementPeriodEnd(periodEnd)
                .totalSales(0)
                .platformFee(0)
                .pgFee(0)
                .settlementAmount(0)
                .bankName(account != null ? account.getBankName() : null)
                .bankAccount(account != null ? account.getBankAccount() : null)
                .build());
    }
}