package com.example.finalproject.settlement.rider.repository;

import com.example.finalproject.settlement.domain.RiderSettlementDetail;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RiderSettlementDetailRepository extends JpaRepository<RiderSettlementDetail, Long> {

    @Query("SELECT d FROM RiderSettlementDetail d JOIN FETCH d.delivery WHERE d.settlement.id = :settlementId")
    List<RiderSettlementDetail> findBySettlementIdWithDelivery(@Param("settlementId") Long settlementId);
}