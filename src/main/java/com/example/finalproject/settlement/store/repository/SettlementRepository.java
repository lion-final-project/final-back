package com.example.finalproject.settlement.store.repository;

import com.example.finalproject.settlement.domain.Settlement;
import com.example.finalproject.settlement.enums.SettlementStatus;
import com.example.finalproject.settlement.enums.SettlementTargetType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    Optional<Settlement> findByTargetTypeAndTargetIdAndSettlementPeriodStartAndSettlementPeriodEnd(
            SettlementTargetType targetType,
            Long targetId,
            LocalDate settlementPeriodStart,
            LocalDate settlementPeriodEnd
    );

    List<Settlement> findByTargetTypeAndTargetIdAndSettlementPeriodStartBetweenOrderBySettlementPeriodStartDesc(
            SettlementTargetType targetType,
            Long targetId,
            LocalDate startInclusive,
            LocalDate endInclusive
    );

    List<Settlement> findByTargetTypeAndStatusAndSettlementPeriodStartAndSettlementPeriodEnd(
            SettlementTargetType targetType,
            SettlementStatus status,
            LocalDate settlementPeriodStart,
            LocalDate settlementPeriodEnd
    );
}
