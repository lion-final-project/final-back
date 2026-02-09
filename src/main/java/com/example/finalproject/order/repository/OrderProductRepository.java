package com.example.finalproject.order.repository;

import com.example.finalproject.order.domain.OrderProduct;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {

    List<OrderProduct> findAllByStoreOrderOrderId(Long orderId);
}
