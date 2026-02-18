package com.example.finalproject.order.repository;

import com.example.finalproject.order.domain.StoreOrder;
import com.example.finalproject.order.enums.OrderType;
import com.example.finalproject.order.enums.StoreOrderStatus;
import com.example.finalproject.order.repository.custom.StoreOrderRepositoryCustom;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoreOrderRepository extends JpaRepository<StoreOrder, Long>, StoreOrderRepositoryCustom {

    List<StoreOrder> findAllByOrderId(Long orderId);

    @Query("SELECT so FROM StoreOrder so JOIN FETCH so.order WHERE so.order.id IN :orderIds")
    List<StoreOrder> findAllByOrderIdIn(@Param("orderIds") List<Long> orderIds);

    List<StoreOrder> findByIdIn(List<Long> ids);

    @Query(value = "SELECT so FROM StoreOrder so JOIN FETCH so.order WHERE so.store.id = :storeId",
            countQuery = "SELECT count(so) FROM StoreOrder so WHERE so.store.id = :storeId")
    Page<StoreOrder> findAllByStoreId(@Param("storeId") Long storeId, Pageable pageable);

    @Query("SELECT so FROM StoreOrder so " +
            "JOIN FETCH so.order " +
            "WHERE so.store.id = :storeId " +
            "AND so.status IN :statuses " +
            "ORDER BY so.createdAt DESC")
    List<StoreOrder> findByStoreIdAndStatusIn(@Param("storeId") Long storeId,
                                              @Param("statuses") List<StoreOrderStatus> statuses);

    @Query("SELECT so FROM StoreOrder so " +
            "JOIN FETCH so.order " +
            "WHERE so.store.id = :storeId " +
            "AND so.status IN :statuses " +
            "ORDER BY so.updatedAt DESC")
    List<StoreOrder> findCompletedByStoreIdAndStatusIn(@Param("storeId") Long storeId,
                                                       @Param("statuses") List<StoreOrderStatus> statuses);

    @Query("SELECT so FROM StoreOrder so " +
            "JOIN FETCH so.order o " +
            "JOIN FETCH o.user " +
            "WHERE so.id = :id")
    Optional<StoreOrder> findByIdWithOrderAndUser(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select so from StoreOrder so "
            + "join fetch so.order o "
            + "join fetch o.user u "
            + "where so.id = :id")
    Optional<StoreOrder> findByIdWithLock(@Param("id") Long id);

    // 매출 조회: 주문 유형별 DELIVERED 건수
    @Query("SELECT COUNT(so) FROM StoreOrder so "
            + "WHERE so.store.id = :storeId "
            + "AND so.status = :status "
            + "AND so.orderType = :orderType "
            + "AND so.deliveredAt BETWEEN :start AND :end")
    long countByStoreIdAndStatusAndOrderTypeAndDeliveredAtBetween(
            @Param("storeId") Long storeId,
            @Param("status") StoreOrderStatus status,
            @Param("orderType") OrderType orderType,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // 매출 조회: DELIVERED 주문 finalPrice 합산
    @Query("SELECT COALESCE(SUM(so.finalPrice), 0) FROM StoreOrder so "
            + "WHERE so.store.id = :storeId "
            + "AND so.status = :status "
            + "AND so.deliveredAt BETWEEN :start AND :end")
    long sumFinalPriceByStoreIdAndStatusAndDeliveredAtBetween(
            @Param("storeId") Long storeId,
            @Param("status") StoreOrderStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // 매출 조회: CANCELLED + REJECTED 건수
    @Query("SELECT COUNT(so) FROM StoreOrder so "
            + "WHERE so.store.id = :storeId "
            + "AND so.status IN :statuses "
            + "AND so.cancelledAt BETWEEN :start AND :end")
    long countByStoreIdAndStatusInAndCancelledAtBetween(
            @Param("storeId") Long storeId,
            @Param("statuses") List<StoreOrderStatus> statuses,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
