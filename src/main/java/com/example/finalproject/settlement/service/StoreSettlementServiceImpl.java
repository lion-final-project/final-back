package com.example.finalproject.settlement.service;

import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.order.domain.StoreOrder;
import com.example.finalproject.order.enums.StoreOrderStatus;
import com.example.finalproject.order.repository.StoreOrderRepository;
import com.example.finalproject.payment.domain.PaymentRefund;
import com.example.finalproject.payment.repository.PaymentRefundRepository;
import com.example.finalproject.settlement.domain.Settlement;
import com.example.finalproject.settlement.domain.SettlementDetail;
import com.example.finalproject.settlement.dto.response.GetStoreSettlementDetailResponse;
import com.example.finalproject.settlement.dto.response.GetStoreSettlementListResponse;
import com.example.finalproject.settlement.enums.SettlementStatus;
import com.example.finalproject.settlement.enums.SettlementTargetType;
import com.example.finalproject.settlement.repository.SettlementDetailRepository;
import com.example.finalproject.settlement.repository.SettlementRepository;
import com.example.finalproject.store.domain.Store;
import com.example.finalproject.store.domain.embedded.SettlementAccount;
import com.example.finalproject.store.enums.StoreStatus;
import com.example.finalproject.store.repository.StoreRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreSettlementServiceImpl implements StoreSettlementService {

    /** 플랫폼 수수료율(요구사항 기준 5%) */
    private static final double PLATFORM_FEE_RATE = 0.05d;

    /** 결제(PG) 수수료율(요구사항 기준 3.3%) */
    private static final double PG_FEE_RATE = 0.033d;

    private final StoreRepository storeRepository;
    private final StoreOrderRepository storeOrderRepository;
    private final SettlementRepository settlementRepository;
    private final SettlementDetailRepository settlementDetailRepository;
    private final PaymentRefundRepository paymentRefundRepository;

    @Override
    public GetStoreSettlementListResponse getSettlements(String ownerEmail, Integer year) {
        // 마트 사장님 계정으로 매장 식별
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

        // 본인 매장 정산만 조회 가능
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
        // 정산 대상 기간: 해당 월 1일 00:00:00 ~ 말일 23:59:59
        LocalDate startDate = targetMonth.atDay(1);
        LocalDate endDate = targetMonth.atEndOfMonth();
        LocalDateTime startAt = startDate.atStartOfDay();
        LocalDateTime endAt = endDate.atTime(LocalTime.MAX);

        // 배달 완료 주문이 1건 이상 있는 매장만 정산 후보
        List<Long> storeIds = storeOrderRepository.findDistinctStoreIdsByStatusAndDeliveredAtBetween(
                StoreOrderStatus.DELIVERED, startAt, endAt);

        for (Long storeId : storeIds) {
            Store store = storeRepository.findById(storeId).orElse(null);

            // 승인 매장만 정산 생성
            if (store == null || store.getStatus() != StoreStatus.APPROVED) {
                continue;
            }

            // 동일 기간 중복 정산 생성 방지
            boolean exists = settlementRepository
                    .findByTargetTypeAndTargetIdAndSettlementPeriodStartAndSettlementPeriodEnd(
                            SettlementTargetType.STORE, storeId, startDate, endDate)
                    .isPresent();
            if (exists) {
                continue;
            }

            List<StoreOrder> deliveredOrders = storeOrderRepository.findByStoreIdAndStatusAndDeliveredAtBetween(
                    storeId, StoreOrderStatus.DELIVERED, startAt, endAt);
            if (deliveredOrders.isEmpty()) {
                continue;
            }

            // 주문별 환불 금액 맵(정산 차감용)
            Map<Long, Integer> refundedByStoreOrder = extractRefundMap(deliveredOrders, startAt, endAt);

            int totalSales = 0;
            int platformFee = 0;
            int pgFee = 0;
            int settlementAmount = 0;

            SettlementAccount account = store.getSettlementAccount();

            // 마스터 먼저 생성 후 상세 라인을 연결한다.
            Settlement settlement = Settlement.builder()
                    .targetType(SettlementTargetType.STORE)
                    .targetId(storeId)
                    .settlementPeriodStart(startDate)
                    .settlementPeriodEnd(endDate)
                    .totalSales(0)
                    .platformFee(0)
                    .pgFee(0)
                    .settlementAmount(0)
                    .bankName(account != null ? account.getBankName() : null)
                    .bankAccount(account != null ? account.getBankAccount() : null)
                    .build();
            settlement = settlementRepository.save(settlement);

            for (StoreOrder storeOrder : deliveredOrders) {
                // 기준 금액은 finalPrice. 환불은 차감해서 실매출(effective) 계산.
                int gross = storeOrder.getFinalPrice();
                int refund = refundedByStoreOrder.getOrDefault(storeOrder.getId(), 0);
                int effective = Math.max(0, gross - refund);

                int platform = (int) Math.round(effective * PLATFORM_FEE_RATE);
                int pg = (int) Math.round(effective * PG_FEE_RATE);
                int fee = platform + pg;
                int net = Math.max(0, effective - fee);

                totalSales += effective;
                platformFee += platform;
                pgFee += pg;
                settlementAmount += net;

                settlementDetailRepository.save(SettlementDetail.builder()
                        .settlement(settlement)
                        .storeOrder(storeOrder)
                        .amount(effective)
                        .fee(fee)
                        .netAmount(net)
                        .build());
            }

            settlement.updateSummary(totalSales, platformFee, pgFee, settlementAmount);
        }
    }

    @Override
    @Transactional
    public int completePendingSettlements(YearMonth targetMonth) {
        LocalDate start = targetMonth.atDay(1);
        LocalDate end = targetMonth.atEndOfMonth();
        List<Settlement> pendings = settlementRepository
                .findByTargetTypeAndStatusAndSettlementPeriodStartAndSettlementPeriodEnd(
                        SettlementTargetType.STORE, SettlementStatus.PENDING, start, end);

        for (Settlement pending : pendings) {
            pending.complete(LocalDateTime.now());
        }

        return pendings.size();
    }

    private Map<Long, Integer> extractRefundMap(
            List<StoreOrder> deliveredOrders,
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {
        // 현재는 환불 기능 팀 작업과 충돌을 피하기 위해 PaymentRefund 전체 이력에서 기간/주문만 필터링한다.
        // TODO(환불 기능 완성 후):
        // 1) REFUND_STATUS = APPROVED만 정산 차감 반영
        // 2) 책임 주체별 차감 로직 분리 (STORE / RIDER / PLATFORM)
        // 3) 부분 환불 정책(전액/부분, 귀속 주체)에 맞춰 정산 항목 세분화
        Set<Long> ids = new HashSet<>(deliveredOrders.stream().map(StoreOrder::getId).toList());
        Map<Long, Integer> map = new HashMap<>();

        for (PaymentRefund refund : paymentRefundRepository.findAll()) {
            if (refund.getStoreOrder() == null || refund.getRefundedAt() == null) {
                continue;
            }
            if (!ids.contains(refund.getStoreOrder().getId())) {
                continue;
            }
            if (refund.getRefundedAt().isBefore(startAt) || refund.getRefundedAt().isAfter(endAt)) {
                continue;
            }
            int amount = refund.getRefundAmount() != null ? refund.getRefundAmount() : 0;
            map.merge(refund.getStoreOrder().getId(), amount, Integer::sum);
        }

        return map;
    }
}
