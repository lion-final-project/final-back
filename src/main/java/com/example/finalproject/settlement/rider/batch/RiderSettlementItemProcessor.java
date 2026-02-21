package com.example.finalproject.settlement.rider.batch;

import com.example.finalproject.settlement.enums.SettlementTargetType;
import com.example.finalproject.settlement.rider.dto.RiderSettlementDto;
import com.example.finalproject.settlement.store.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

/**
 * 라이더 주간 정산 ItemProcessor.
 * <p>
 * 역할:
 * <ul>
 *   <li>중복 정산 방지 — 동일 라이더 + 동일 기간의 Settlement 레코드가 이미 존재하면 {@code null} 반환 (skip)</li>
 *   <li>유효한 경우 DTO를 그대로 Writer로 전달 (금액 계산은 Writer 담당)</li>
 * </ul>
 * </p>
 */
@Slf4j
@RequiredArgsConstructor
public class RiderSettlementItemProcessor implements ItemProcessor<RiderSettlementDto, RiderSettlementDto> {

    private final SettlementRepository settlementRepository;

    @Override
    public RiderSettlementDto process(RiderSettlementDto dto) {
        boolean exists = settlementRepository
                .findByTargetTypeAndTargetIdAndSettlementPeriodStartAndSettlementPeriodEnd(
                        SettlementTargetType.RIDER,
                        dto.getRiderId(),
                        dto.getPeriodStart(),
                        dto.getPeriodEnd())
                .isPresent();

        if (exists) {
            log.warn("[RiderSettlement] 중복 정산 skip — riderId={}, period={}~{}",
                    dto.getRiderId(), dto.getPeriodStart(), dto.getPeriodEnd());
            return null;
        }

        return dto;
    }
}