package com.example.finalproject.settlement.store.repository;

import com.example.finalproject.settlement.domain.SettlementDetail;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SettlementDetailRepository extends JpaRepository<SettlementDetail, Long> {

    @Query("SELECT sd FROM SettlementDetail sd "
            + "JOIN FETCH sd.storeOrder so "
            + "JOIN FETCH so.order o "
            + "WHERE sd.settlement.id = :settlementId "
            + "ORDER BY sd.id DESC")
    List<SettlementDetail> findAllBySettlementIdWithStoreOrder(@Param("settlementId") Long settlementId);

    void deleteBySettlementId(Long settlementId);
}
