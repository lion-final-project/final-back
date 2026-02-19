package com.example.finalproject.delivery.repository;

import com.example.finalproject.delivery.domain.Delivery;
import com.example.finalproject.delivery.domain.Rider;
import com.example.finalproject.delivery.enums.DeliveryStatus;
import com.example.finalproject.payment.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /** 매장주문(StoreOrder) ID로 배달 조회 (배달 생성 시 중복 확인용) */
    Optional<Delivery> findByStoreOrderId(Long storeOrderId);

    /** 여러 매장주문 ID로 배달 목록 조회 */
    java.util.List<Delivery> findByStoreOrderIdIn(java.util.List<Long> storeOrderIds);

    /** 라이더가 특정 상태들 중 하나에 해당하는 배달을 보유 중인지 확인 */
    boolean existsByRiderAndStatusIn(Rider rider, java.util.List<DeliveryStatus> statuses);

    /** 라이더의 특정 상태 배달 건수를 조회 (동시 배달 제한 확인용) */
    long countByRiderAndStatusIn(Rider rider, java.util.List<DeliveryStatus> statuses);

    @Query(value = """
            SELECT d
            FROM Delivery d
            JOIN d.storeOrder so
            JOIN so.order o
            JOIN o.user u
            JOIN Payment p ON p.order = o
            WHERE u.id = :userId
              AND p.paymentStatus = :paymentStatus
              AND d.status IN :statuses
            ORDER BY d.createdAt DESC
            """,
            countQuery = """
                    SELECT COUNT(d)
                    FROM Delivery d
                    JOIN d.storeOrder so
                    JOIN so.order o
                    JOIN o.user u
                    JOIN Payment p ON p.order = o
                    WHERE u.id = :userId
                      AND p.paymentStatus = :paymentStatus
                      AND d.status IN :statuses
                    """)
    Page<Delivery> findTrackableByUserIdAndStatuses(@Param("userId") Long userId,
                                                    @Param("paymentStatus") PaymentStatus paymentStatus,
                                                    @Param("statuses") java.util.List<DeliveryStatus> statuses,
                                                    Pageable pageable);

    Optional<Delivery> findByIdAndStoreOrderOrderUserId(Long deliveryId, Long userId);
}
