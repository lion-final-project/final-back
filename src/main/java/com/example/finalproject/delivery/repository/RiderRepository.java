package com.example.finalproject.delivery.repository;

import com.example.finalproject.delivery.domain.Rider;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RiderRepository extends JpaRepository<Rider, Long> {
    /**
     * 특정 사용자가 이미 라이더로 등록되어 있는지 확인
     */
    boolean existsByUserId(Long userId);

    /**
     * 사용자 ID로 라이더 정보 조회
     */
    Optional<Rider> findByUserId(Long userId);


}
