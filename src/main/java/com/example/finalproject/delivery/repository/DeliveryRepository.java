package com.example.finalproject.delivery.repository;

import com.example.finalproject.delivery.domain.Delivery;
import com.example.finalproject.delivery.domain.Rider;
import com.example.finalproject.delivery.enums.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 배달 도메인 레포지토리.
 * 라이더별 배달 조회, 상태별 필터링, 주문 기반 매핑 등을 지원합니다.
 */
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    /** 특정 라이더의 모든 배달을 페이징 조회 */
    Page<Delivery> findByRider(Rider rider, Pageable pageable);

    /** 특정 라이더의 배달을 상태별로 필터링하여 페이징 조회 */
    Page<Delivery> findByRiderAndStatus(Rider rider, DeliveryStatus status, Pageable pageable);

    /** 매장주문(StoreOrder) ID로 배달 조회 (배달 생성 시 중복 랩도 확인용) */
    Optional<Delivery> findByStoreOrderId(Long storeOrderId);

    /** 라이더가 특정 상태들 중 하나에 해당하는 배달을 보유 중인지 확인 */
    boolean existsByRiderAndStatusIn(Rider rider, java.util.List<DeliveryStatus> statuses);

    /** 라이더의 특정 상태 배달 건수를 조회 (동시 배달 제한 확인용) */
    long countByRiderAndStatusIn(Rider rider, java.util.List<DeliveryStatus> statuses);
}
