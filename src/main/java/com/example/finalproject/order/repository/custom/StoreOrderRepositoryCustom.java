package com.example.finalproject.order.repository.custom;

import com.example.finalproject.order.domain.StoreOrder;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StoreOrderRepositoryCustom {
    public Page<StoreOrder> findUserStoreOrders(
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String searchTerm,
            Pageable pageable
    );
}
