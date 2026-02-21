package com.example.finalproject.settlement.rider.service;

import com.example.finalproject.delivery.domain.Rider;
import com.example.finalproject.delivery.repository.RiderRepository;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.settlement.domain.RiderSettlementDetail;
import com.example.finalproject.settlement.domain.Settlement;
import com.example.finalproject.settlement.enums.SettlementTargetType;
import com.example.finalproject.settlement.rider.dto.response.GetRiderSettlementDetailResponse;
import com.example.finalproject.settlement.rider.dto.response.GetRiderSettlementListResponse;
import com.example.finalproject.settlement.rider.repository.RiderSettlementDetailRepository;
import com.example.finalproject.settlement.store.repository.SettlementRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RiderSettlementQueryService {

    private final RiderRepository riderRepository;
    private final SettlementRepository settlementRepository;
    private final RiderSettlementDetailRepository riderSettlementDetailRepository;

    public GetRiderSettlementListResponse getSettlements(String email, Integer year, Integer month) {
        Rider rider = findRiderByEmail(email);

        LocalDate start;
        LocalDate end;

        if (year != null && month != null) {
            start = LocalDate.of(year, month, 1);
            end = start.withDayOfMonth(start.lengthOfMonth());
        } else if (year != null) {
            start = LocalDate.of(year, 1, 1);
            end = LocalDate.of(year, 12, 31);
        } else {
            end = LocalDate.now();
            start = end.minusMonths(6).withDayOfMonth(1);
        }

        List<GetRiderSettlementListResponse.Item> content = settlementRepository
                .findByTargetTypeAndTargetIdAndSettlementPeriodStartBetweenOrderBySettlementPeriodStartDesc(
                        SettlementTargetType.RIDER, rider.getId(), start, end)
                .stream()
                .map(GetRiderSettlementListResponse.Item::from)
                .toList();

        return GetRiderSettlementListResponse.builder().content(content).build();
    }

    public GetRiderSettlementDetailResponse getSettlementDetail(String email, Long settlementId) {
        Rider rider = findRiderByEmail(email);

        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        if (settlement.getTargetType() != SettlementTargetType.RIDER
                || !settlement.getTargetId().equals(rider.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        List<RiderSettlementDetail> details =
                riderSettlementDetailRepository.findBySettlementIdWithDelivery(settlementId);

        return GetRiderSettlementDetailResponse.of(settlement, details);
    }

    private Rider findRiderByEmail(String email) {
        return riderRepository.findByUserEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.RIDER_NOT_FOUND));
    }
}
