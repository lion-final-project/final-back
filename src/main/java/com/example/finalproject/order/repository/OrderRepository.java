package com.example.finalproject.order.repository;

import com.example.finalproject.order.domain.Order;
import com.example.finalproject.order.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdAndUserId(Long orderId, Long userId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.orderedAt >= :start AND o.orderedAt < :end")
    long countByOrderedAtBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    long countByUserIdAndStatusIn(Long userId, Collection<OrderStatus> statuses);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.orderedAt DESC")
    Page<Order> findAllByUserIdOrderByOrderedAtDesc(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.orderedAt DESC")
    List<Order> findAllByUserIdOrderByOrderedAtDesc(@Param("userId") Long userId);
}
