package com.example.finalproject.order.repository;

import com.example.finalproject.order.domain.StoreOrder;
import com.example.finalproject.order.enums.StoreOrderStatus;
import com.example.finalproject.order.repository.custom.StoreOrderRepositoryCustom;
import jakarta.persistence.LockModeType;
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
}
