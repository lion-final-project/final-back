package com.example.finalproject.settlement.rider.batch;

import com.example.finalproject.delivery.domain.Delivery;
import com.example.finalproject.delivery.domain.QDelivery;
import com.example.finalproject.delivery.domain.QRider;
import com.example.finalproject.delivery.domain.Rider;
import com.example.finalproject.delivery.enums.DeliveryStatus;
import com.example.finalproject.payment.domain.PaymentRefund;
import com.example.finalproject.payment.domain.QPaymentRefund;
import com.example.finalproject.payment.enums.RefundResponsibility;
import com.example.finalproject.payment.enums.RefundStatus;
import com.example.finalproject.settlement.rider.dto.RiderSettlementDto;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;

/**
 * 라이더 주간 정산 ItemReader.
 *
 * <p>직전 주(月~日) DELIVERED 이고 isSettled=false 인 배달 건을 보유한 라이더를
 * chunkSize 단위로 페이지 조회하여 {@link RiderSettlementDto} 를 반환한다.</p>
 *
 * <p>쿼리 전략 (4-Query 방식, Java-side 집계):</p>
 * <ol>
 *   <li>대상 라이더 ID 페이지 조회 (GROUP BY rider_id)</li>
 *   <li>해당 라이더들의 배달 목록 조회 (rider fetchJoin) → earningMap + deliveryMap 집계</li>
 *   <li>배달 storeOrder ID 기준 PaymentRefund 조회 → refundMap 집계</li>
 *   <li>라이더 계좌 메타 조회</li>
 * </ol>
 *
 * <p>QueryDSL 6.x openfeign fork의 다중 컬럼 select·집계 함수 API 변경으로 인해
 * SQL-level SUM/Tuple 대신 Java stream 집계를 사용한다.</p>
 */
@RequiredArgsConstructor
public class RiderSettlementItemReader implements ItemReader<RiderSettlementDto> {

    private final JPAQueryFactory queryFactory;
    private final int chunkSize;
    private final LocalDateTime weekStart;
    private final LocalDateTime weekEnd;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;

    private Iterator<RiderSettlementDto> iterator;
    private int offset = 0;
    private boolean exhausted = false;

    @Override
    public RiderSettlementDto read() {
        if (exhausted) return null;

        if (iterator == null || !iterator.hasNext()) {
            List<RiderSettlementDto> page = fetchPage(offset, chunkSize);
            if (page.isEmpty()) {
                exhausted = true;
                return null;
            }
            iterator = page.iterator();
            offset += page.size();
        }

        return iterator.hasNext() ? iterator.next() : null;
    }

    private List<RiderSettlementDto> fetchPage(int offset, int limit) {
        QDelivery d = QDelivery.delivery;
        QRider r = QRider.rider;
        QPaymentRefund pr = QPaymentRefund.paymentRefund;

        // ① 대상 라이더 ID 페이지 조회 (riderEarning null 제외)
        List<Long> riderIds = queryFactory
                .select(d.rider.id)
                .from(d)
                .where(
                        d.status.eq(DeliveryStatus.DELIVERED),
                        d.deliveredAt.between(weekStart, weekEnd),
                        d.isSettled.isFalse(),
                        d.rider.isNotNull(),
                        d.riderEarning.isNotNull()
                )
                .groupBy(d.rider.id)
                .orderBy(d.rider.id.asc())
                .offset(offset)
                .limit(limit)
                .fetch();

        if (riderIds.isEmpty()) return List.of();

        // ② 해당 라이더들의 배달 목록 조회 (rider fetchJoin으로 LazyLoad 방지)
        List<Delivery> deliveries = queryFactory
                .selectFrom(d)
                .join(d.rider, r).fetchJoin()
                .where(
                        d.rider.id.in(riderIds),
                        d.status.eq(DeliveryStatus.DELIVERED),
                        d.deliveredAt.between(weekStart, weekEnd),
                        d.isSettled.isFalse(),
                        d.riderEarning.isNotNull()
                )
                .fetch();

        // earningMap: riderId → riderEarning 합계
        Map<Long, Integer> earningMap = deliveries.stream()
                .collect(Collectors.groupingBy(
                        del -> del.getRider().getId(),
                        Collectors.summingInt(del -> del.getRiderEarning() != null ? del.getRiderEarning() : 0)
                ));

        // deliveryMap: riderId → 배달 ID 목록 (Writer 벌크 UPDATE용)
        Map<Long, List<Long>> deliveryMap = deliveries.stream()
                .collect(Collectors.groupingBy(
                        del -> del.getRider().getId(),
                        Collectors.mapping(Delivery::getId, Collectors.toList())
                ));

        // ③ 라이더 귀책 환불 집계 — storeOrder ID 기준
        // storeOrderId → riderId 역매핑 (deliveries에서 구성)
        Set<Long> storeOrderIds = deliveries.stream()
                .filter(del -> del.getStoreOrder() != null)
                .map(del -> del.getStoreOrder().getId())
                .collect(Collectors.toSet());

        Map<Long, Long> storeOrderToRiderMap = deliveries.stream()
                .filter(del -> del.getStoreOrder() != null && del.getRider() != null)
                .collect(Collectors.toMap(
                        del -> del.getStoreOrder().getId(),
                        del -> del.getRider().getId(),
                        (a, b) -> a
                ));

        Map<Long, Integer> refundMap = new HashMap<>();
        if (!storeOrderIds.isEmpty()) {
            List<PaymentRefund> refunds = queryFactory
                    .selectFrom(pr)
                    .where(
                            pr.storeOrder.id.in(storeOrderIds),
                            pr.responsibility.eq(RefundResponsibility.RIDER),
                            pr.refundStatus.eq(RefundStatus.APPROVED),
                            pr.isSettled.isFalse()
                    )
                    .fetch();

            for (PaymentRefund refund : refunds) {
                if (refund.getStoreOrder() == null) continue;
                Long riderId = storeOrderToRiderMap.get(refund.getStoreOrder().getId());
                if (riderId != null) {
                    refundMap.merge(riderId,
                            refund.getRefundAmount() != null ? refund.getRefundAmount() : 0,
                            Integer::sum);
                }
            }
        }

        // ④ 라이더 계좌 메타 조회
        Map<Long, Rider> riderMetaMap = queryFactory
                .selectFrom(r)
                .where(r.id.in(riderIds))
                .fetch()
                .stream()
                .collect(Collectors.toMap(Rider::getId, Function.identity()));

        return riderIds.stream()
                .map(riderId -> {
                    Rider riderMeta = riderMetaMap.get(riderId);
                    return RiderSettlementDto.builder()
                            .riderId(riderId)
                            .bankName(riderMeta != null ? riderMeta.getBankName() : null)
                            .bankAccount(riderMeta != null ? riderMeta.getBankAccount() : null)
                            .accountHolder(riderMeta != null ? riderMeta.getAccountHolder() : null)
                            .totalEarning(earningMap.getOrDefault(riderId, 0))
                            .totalRefund(refundMap.getOrDefault(riderId, 0))
                            .periodStart(periodStart)
                            .periodEnd(periodEnd)
                            .deliveryIds(deliveryMap.getOrDefault(riderId, List.of()))
                            .build();
                })
                .toList();
    }
}
