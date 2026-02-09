package com.example.finalproject.order.repository;

import com.example.finalproject.order.domain.StoreOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreOrderRepository extends JpaRepository<StoreOrder, Long> {
}
