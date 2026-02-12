package com.example.finalproject.order.repository;

import com.example.finalproject.order.domain.Order;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

import com.example.finalproject.order.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdAndUserId(Long orderId, Long userId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderedAt >= :start AND o.orderedAt < :end")
    long countByOrderedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    long countByUserIdAndStatusIn(Long userId, Collection<OrderStatus> statuses);
}
