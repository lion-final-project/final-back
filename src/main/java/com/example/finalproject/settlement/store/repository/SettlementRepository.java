package com.example.finalproject.settlement.store.repository;

import com.example.finalproject.settlement.domain.Settlement;
import com.example.finalproject.settlement.enums.SettlementStatus;
import com.example.finalproject.settlement.enums.SettlementTargetType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    List<Settlement> findByTargetTypeAndSettlementPeriodStartBetweenOrderBySettlementPeriodStartDesc(
            SettlementTargetType targetType,
            LocalDate startInclusive,
            LocalDate endInclusive
    );

    List<Settlement> findByTargetTypeAndStatusAndSettlementPeriodStartAndSettlementPeriodEnd(
            SettlementTargetType targetType,
            SettlementStatus status,
            LocalDate settlementPeriodStart,
            LocalDate settlementPeriodEnd
    );

    Page<Settlement> findByTargetTypeAndSettlementPeriodStartBetween(
            SettlementTargetType targetType,
            LocalDate startInclusive,
            LocalDate endInclusive,
            Pageable pageable
    );

    Page<Settlement> findByTargetTypeAndStatusAndSettlementPeriodStartBetween(
            SettlementTargetType targetType,
            SettlementStatus status,
            LocalDate startInclusive,
            LocalDate endInclusive,
            Pageable pageable
    );

    List<Settlement> findByTargetTypeAndStatusAndSettlementPeriodStartBetweenOrderBySettlementPeriodStartDesc(
            SettlementTargetType targetType,
            SettlementStatus status,
            LocalDate startInclusive,
            LocalDate endInclusive
    );

    long countByTargetTypeAndStatus(SettlementTargetType targetType, SettlementStatus status);

    @Query("SELECT COALESCE(SUM(s.totalSales), 0) FROM Settlement s "
            + "WHERE s.targetType = :targetType "
            + "AND s.settlementPeriodStart BETWEEN :startInclusive AND :endInclusive")
    long sumTotalSalesByTargetTypeAndPeriod(
            @Param("targetType") SettlementTargetType targetType,
            @Param("startInclusive") LocalDate startInclusive,
            @Param("endInclusive") LocalDate endInclusive
    );

    @Query("SELECT COALESCE(SUM(s.settlementAmount), 0) FROM Settlement s "
            + "WHERE s.targetType = :targetType "
            + "AND s.settlementPeriodStart BETWEEN :startInclusive AND :endInclusive")
    long sumSettlementAmountByTargetTypeAndPeriod(
            @Param("targetType") SettlementTargetType targetType,
            @Param("startInclusive") LocalDate startInclusive,
            @Param("endInclusive") LocalDate endInclusive
    );
}
