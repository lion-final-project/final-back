package com.example.finalproject.product.repository;

import com.example.finalproject.product.domain.ProductStockHistory;
import com.example.finalproject.product.domain.StockEventType;
import com.example.finalproject.store.domain.Store;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductStockHistoryRepository extends JpaRepository<ProductStockHistory, Long> {

    @Query("SELECT COUNT(h) FROM ProductStockHistory h " +
            "WHERE h.product.store = :store " +
            "AND h.eventType = :eventType " +
            "AND h.createdAt >= :startOfDay")
    Long countByStoreAndEventTypeAndCreatedAtAfter(@Param("store") Store store,
                                                   @Param("eventType") StockEventType eventType,
                                                   @Param("startOfDay") LocalDateTime startOfDay);

    @Query(value = "SELECT h FROM ProductStockHistory h " +
            "JOIN FETCH h.product " +
            "WHERE h.product.store = :store",
            countQuery = "SELECT COUNT(h) FROM ProductStockHistory h WHERE h.product.store = :store")
    Page<ProductStockHistory> findByStore(@Param("store") Store store, Pageable pageable);
}
