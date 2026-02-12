package com.example.finalproject.delivery.repository;

import com.example.finalproject.delivery.domain.Rider;
import com.example.finalproject.delivery.domain.RiderLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 라이더 위치 이력 레포지토리.
 * <p>
 * DB 기반 위치 이력 추적용입니다 (Redis GEO는 실시간 위치, 여기는 이력 보관).
 * 활성 배달 중 위치 기록을 저장하여 배송 경로 추적에 활용합니다.
 * </p>
 */
public interface RiderLocationRepository extends JpaRepository<RiderLocation, Long> {

    /** 특정 라이더의 특정 배달 고의 위치 이력을 최신순으로 조회 */
    List<RiderLocation> findByRiderAndDeliveryIdOrderByRecordedAtDesc(Rider rider, Long deliveryId);

    /** 현재 위치로 표시된 레코드 조회 */
    List<RiderLocation> findByRiderAndIsCurrentTrue(Rider rider);

    /** 현재 위치 레코드 삭제 (새 위치로 갱신 시 기존 위치 제거용) */
    void deleteByRiderAndIsCurrentTrue(Rider rider);
}
