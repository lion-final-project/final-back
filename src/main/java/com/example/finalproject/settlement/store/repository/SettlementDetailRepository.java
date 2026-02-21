package com.example.finalproject.settlement.store.repository;

import com.example.finalproject.settlement.domain.SettlementDetail;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementDetailRepository extends JpaRepository<SettlementDetail, Long> {

    List<SettlementDetail> findBySettlementId(Long settlementId);

    void deleteBySettlementId(Long settlementId);
}
