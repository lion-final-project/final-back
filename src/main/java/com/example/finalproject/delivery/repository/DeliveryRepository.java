package com.example.finalproject.delivery.repository;

import com.example.finalproject.delivery.domain.Delivery;
import com.example.finalproject.delivery.domain.Rider;
import com.example.finalproject.delivery.enums.DeliveryStatus;
import com.example.finalproject.delivery.repository.custom.DeliveryRepositoryCustom;
import com.example.finalproject.payment.enums.PaymentStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 배달 관련 조회/매핑 저장소.
 * 라이더별 배달 조회, 상태 필터링, 주문 기반 매핑 메서드를 제공한다.
 */
public interface DeliveryRepository extends JpaRepository<Delivery, Long>, DeliveryRepositoryCustom {

  /** 특정 라이더의 모든 배달을 페이지로 조회 */
  Page<Delivery> findByRider(Rider rider, Pageable pageable);

  /** 특정 라이더의 배달을 상태별로 필터링하여 페이지 조회 */
  Page<Delivery> findByRiderAndStatus(Rider rider, DeliveryStatus status, Pageable pageable);

  /** 특정 라이더의 배달을 여러 상태 중 하나로 필터링하여 페이지 조회 */
  Page<Delivery> findByRiderAndStatusIn(Rider rider, List<DeliveryStatus> statuses, Pageable pageable);

  /** 스토어 주문 ID로 배달 조회 (배달 생성 전 중복 확인용) */
  Optional<Delivery> findByStoreOrderId(Long storeOrderId);

  /** 여러 스토어 주문 ID로 배달 목록 조회 */
  List<Delivery> findByStoreOrderIdIn(List<Long> storeOrderIds);

  /** 라이더가 특정 상태 중 하나에 해당하는 배달을 보유 중인지 확인 */
  boolean existsByRiderAndStatusIn(Rider rider, List<DeliveryStatus> statuses);

  /** 라이더의 특정 상태 배달 건수를 조회 (동시 배달 제한 확인용) */
  long countByRiderAndStatusIn(Rider rider, List<DeliveryStatus> statuses);

  Optional<Delivery> findByIdAndStoreOrderOrderUserId(Long deliveryId, Long userId);

//  @Query(value = """
//      SELECT d
//      FROM Delivery d
//      JOIN d.storeOrder so
//      JOIN so.order o
//      JOIN o.user u
//      JOIN Payment p ON p.order = o
//      WHERE u.id = :userId
//        AND p.paymentStatus = :paymentStatus
//        AND d.status IN :statuses
//      ORDER BY d.createdAt DESC
//      """, countQuery = """
//      SELECT COUNT(d)
//      FROM Delivery d
//      JOIN d.storeOrder so
//      JOIN so.order o
//      JOIN o.user u
//      JOIN Payment p ON p.order = o
//      WHERE u.id = :userId
//        AND p.paymentStatus = :paymentStatus
//        AND d.status IN :statuses
//      """)
//  Page<Delivery> findTrackableByUserIdAndStatuses(@Param("userId") Long userId,
//      @Param("paymentStatus") PaymentStatus paymentStatus,
//      @Param("statuses") List<DeliveryStatus> statuses,
//      Pageable pageable);

  @Query("""
      SELECT COUNT(DISTINCT d.rider.id)
      FROM Delivery d
      WHERE d.status = :status
        AND d.rider IS NOT NULL
      """)
  long countDistinctRiderByStatus(@Param("status") DeliveryStatus status);
}
