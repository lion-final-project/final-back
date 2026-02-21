package com.example.finalproject.order.service;

import com.example.finalproject.order.domain.StoreOrder;
import com.example.finalproject.order.enums.StoreOrderStatus;
import com.example.finalproject.order.repository.StoreOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreOrderAutoReadyService {

    private final StoreOrderRepository storeOrderRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markReadySingleOrder(Long storeOrderId) {
        StoreOrder storeOrder = storeOrderRepository.findById(storeOrderId)
                .orElseThrow(() -> new IllegalStateException("주문을 찾을 수 없습니다: " + storeOrderId));

        if (storeOrder.getStatus() != StoreOrderStatus.ACCEPTED) {
            return;
        }

        storeOrder.markReady();
    }
}
