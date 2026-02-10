package com.example.finalproject.order.repository;

import com.example.finalproject.order.domain.OrderProduct;

import java.util.Collection;
import java.util.List;

import com.example.finalproject.order.domain.StoreOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {

    List<OrderProduct> findAllByStoreOrderOrderId(Long orderId);

    List<OrderProduct> findByStoreOrderIn(Collection<StoreOrder> storeOrders);
}
