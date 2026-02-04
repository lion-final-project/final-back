package com.example.finalproject.delivery.repository;

import com.example.finalproject.delivery.domain.Rider;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// 라이더 엔티티 기본 리포지토리.
public interface RiderRepository extends JpaRepository<Rider, Long> {

    // 사용자 ID로 라이더 단건 조회.
    Optional<Rider> findByUserId(Long userId);
}
