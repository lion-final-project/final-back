package com.example.finalproject.store.repository;

import com.example.finalproject.store.domain.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {

    /**
     * 소유자(owner) ID로 마트를 조회한다.
     *
     * @param ownerId 사용자(사장) ID
     * @return 해당 사용자가 소유한 마트 (0 또는 1건)
     */
    Optional<Store> findByOwner_Id(Long ownerId);
}
