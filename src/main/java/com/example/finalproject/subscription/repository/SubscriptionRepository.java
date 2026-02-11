package com.example.finalproject.subscription.repository;

import com.example.finalproject.subscription.domain.Subscription;
import com.example.finalproject.subscription.domain.SubscriptionProduct;
import com.example.finalproject.subscription.enums.SubscriptionStatus;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    /**
     * 마트(store)별로 특정 상태 집합에 속하는 구독 목록을 조회한다.
     *
     * @param storeId  마트 ID
     * @param statuses 조회할 상태 집합 (예: ACTIVE)
     * @return 구독 목록
     */
    List<Subscription> findByStoreIdAndStatusIn(Long storeId, Collection<SubscriptionStatus> statuses);

    /**
     * 고객(사용자)의 구독 목록을 조회한다. 해지 완료(CANCELLED)는 제외하고, 최신순 정렬 (UC-C10).
     *
     * @param userId   사용자 ID
     * @param statuses 조회할 상태 집합 (ACTIVE, PAUSED, CANCELLATION_PENDING)
     * @return 구독 목록
     */
    List<Subscription> findByUserIdAndStatusInOrderByCreatedAtDesc(Long userId,
                                                                   Collection<SubscriptionStatus> statuses);

    /**
     * 구독 ID와 소유 사용자 ID로 구독을 조회한다. 본인 구독 여부 검증용.
     *
     * @param id     구독 ID
     * @param userId 사용자 ID
     * @return 구독 (Optional)
     */
    Optional<Subscription> findByIdAndUserId(Long id, Long userId);

    /**
     * 구독 상품별로 활성(ACTIVE) 구독자 수를 센다.
     *
     * @param subscriptionProduct 구독 상품
     * @param status              구독 상태 (ACTIVE 등)
     * @return 해당 상태의 구독 건수
     */
    long countBySubscriptionProductAndStatus(SubscriptionProduct subscriptionProduct, SubscriptionStatus status);

    /**
     * 구독 상품에 대해 특정 상태 집합에 속하는 구독이 존재하는지 여부를 확인한다.
     *
     * @param subscriptionProduct 구독 상품
     * @param statuses            구독 상태 목록
     * @return true: 존재, false: 없음
     */
    boolean existsBySubscriptionProductAndStatusIn(SubscriptionProduct subscriptionProduct,
                                                   Collection<SubscriptionStatus> statuses);

    /**
     * 구독 상품에 대해 특정 상태 집합에 속한 구독 건수를 계산한다.
     *
     * @param subscriptionProduct 구독 상품
     * @param statuses            구독 상태 목록
     * @return 해당 상태 집합에 속한 구독 수
     */
    long countBySubscriptionProductAndStatusIn(SubscriptionProduct subscriptionProduct,
                                               Collection<SubscriptionStatus> statuses);

    @Query("select s.id "
            + "from Subscription s "
            + "where s.status = :status "
            + "and s.nextPaymentDate <= :today")
    List<Long> findIdsByStatusAndNextPaymentDateLessThanEqual(SubscriptionStatus status, LocalDate today);

}
