package com.example.finalproject.order.repository;

import com.example.finalproject.order.domain.OrderProduct;
import com.example.finalproject.order.domain.StoreOrder;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderProductRepository extends JpaRepository<OrderProduct, Long> {

    List<OrderProduct> findAllByStoreOrderOrderId(Long orderId);

    List<OrderProduct> findByStoreOrderIn(Collection<StoreOrder> storeOrders);
    
    @Query("select op "
            + "from OrderProduct op "
            + "join fetch op.product "
            + "where op.storeOrder.id in :ids")
    List<OrderProduct> findByStoreOrderIdInWithProduct(@Param("ids") List<Long> ids);

    List<OrderProduct> findAllByStoreOrderId(Long storeOrderId);
}
