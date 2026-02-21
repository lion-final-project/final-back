package com.example.finalproject.settlement.rider.batch;

import com.example.finalproject.delivery.domain.Delivery;
import com.example.finalproject.delivery.repository.DeliveryRepository;
import com.example.finalproject.settlement.domain.RiderSettlementDetail;
import com.example.finalproject.settlement.domain.Settlement;
import com.example.finalproject.settlement.enums.SettlementTargetType;
import com.example.finalproject.settlement.rider.dto.RiderSettlementDto;
import com.example.finalproject.settlement.rider.repository.RiderSettlementDetailRepository;
import com.example.finalproject.settlement.store.repository.SettlementRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 라이더 주간 정산 ItemWriter.
 *
 * <p>
 * 처리 순서 (라이더 1명 단위):
 * </p>
 * <ol>
 * <li>Settlement 마스터 레코드 생성 및 저장</li>
 * <li>RiderSettlementDetail 레코드 생성 및 저장 (배달 건별 스냅샷)</li>
 * <li>deliveries.is_settled = true + settlement_id 벌크 UPDATE
 * (JdbcTemplate)</li>
 * <li>payment_refunds.is_settled = true 벌크 UPDATE — 해당 배달의 RIDER 귀책·APPROVED
 * 환불만 (JdbcTemplate)</li>
 * </ol>
 *
 * <p>
 * 트랜잭션은 Spring Batch Step이 관리하므로 별도 {@code @Transactional} 불필요.
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class RiderSettlementItemWriter implements ItemWriter<RiderSettlementDto> {

    private final SettlementRepository settlementRepository;
    private final RiderSettlementDetailRepository riderSettlementDetailRepository;
    private final DeliveryRepository deliveryRepository;
    private final JdbcTemplate jdbcTemplate;

    private static final double PLATFORM_FEE_RATE = 0.05d;
    private static final double PG_FEE_RATE = 0.033d;

    @Override
    public void write(Chunk<? extends RiderSettlementDto> chunk) {
        for (RiderSettlementDto dto : chunk) {
            int totalEarning = dto.getTotalEarning();
            int platformFee = (int) (totalEarning * PLATFORM_FEE_RATE);
            int pgFee = (int) (totalEarning * PG_FEE_RATE);

            int netAmount = Math.max(0, totalEarning - platformFee - pgFee - dto.getTotalRefund());

            // ① Settlement 마스터 저장
            Settlement settlement = settlementRepository.save(
                    Settlement.builder()
                            .targetType(SettlementTargetType.RIDER)
                            .targetId(dto.getRiderId())
                            .settlementPeriodStart(dto.getPeriodStart())
                            .settlementPeriodEnd(dto.getPeriodEnd())
                            .totalSales(dto.getTotalEarning())
                            .platformFee(platformFee)
                            .pgFee(pgFee)
                            .settlementAmount(netAmount)
                            .bankName(dto.getBankName())
                            .bankAccount(dto.getBankAccount())
                            .build());
            settlement.complete(LocalDateTime.now());

            // ② RiderSettlementDetail 저장 (배달 건별 수익 스냅샷)
            List<Long> deliveryIds = dto.getDeliveryIds();
            if (!deliveryIds.isEmpty()) {
                List<Delivery> deliveries = deliveryRepository.findAllById(deliveryIds);
                List<RiderSettlementDetail> details = deliveries.stream()
                        .map(d -> {
                            int earning = d.getRiderEarning() != null ? d.getRiderEarning() : 0;
                            return RiderSettlementDetail.builder()
                                    .settlement(settlement)
                                    .delivery(d)
                                    .riderEarning(earning)
                                    .refundAmount(0) // 건별 환불 세분화는 추후 확장
                                    .netAmount(earning)
                                    .build();
                        })
                        .toList();
                riderSettlementDetailRepository.saveAll(details);
            }

            // ③ Delivery 벌크 업데이트 (is_settled = true, settlement_id 연결)
            if (!deliveryIds.isEmpty()) {
                Long[] idArray = deliveryIds.toArray(Long[]::new);
                final long settlementId = settlement.getId();

                jdbcTemplate.update(con -> {
                    var ps = con.prepareStatement(
                            "UPDATE deliveries SET is_settled = true, settlement_id = ? WHERE id = ANY(?)");
                    ps.setLong(1, settlementId);
                    ps.setArray(2, con.createArrayOf("bigint", idArray));
                    return ps;
                });

                log.info("[RiderSettlement] Delivery {}건 is_settled 업데이트 — riderId={}",
                        idArray.length, dto.getRiderId());
            }

            // ④ PaymentRefund 벌크 업데이트
            // 대상 배달 ID 목록의 store_order_id 와 연결된 RIDER 귀책·APPROVED·미정산 환불만 처리.
            // rider_id 기준이 아닌 delivery ID 기준으로 범위를 제한하여 기간 외 환불 오염을 방지.
            if (!deliveryIds.isEmpty()) {
                Long[] idArray = deliveryIds.toArray(Long[]::new);

                jdbcTemplate.update(con -> {
                    var ps = con.prepareStatement(
                            "UPDATE payment_refunds pr " +
                                    "SET is_settled = true " +
                                    "WHERE pr.store_order_id IN (" +
                                    "    SELECT d.store_order_id FROM deliveries d WHERE d.id = ANY(?)" +
                                    ") " +
                                    "AND pr.refund_responsibility = 'RIDER' " +
                                    "AND pr.refund_status = 'APPROVED' " +
                                    "AND pr.is_settled = false");
                    ps.setArray(1, con.createArrayOf("bigint", idArray));
                    return ps;
                });
            }

            log.info(
                    "[RiderSettlement] 정산 완료 — riderId={}, period={}~{}, totalEarning={}, totalRefund={}, netAmount={}",
                    dto.getRiderId(), dto.getPeriodStart(), dto.getPeriodEnd(),
                    dto.getTotalEarning(), dto.getTotalRefund(), netAmount);
        }
    }
}
