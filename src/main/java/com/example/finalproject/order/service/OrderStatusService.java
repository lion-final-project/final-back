package com.example.finalproject.order.service;

import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.order.domain.Order;
import com.example.finalproject.order.domain.OrderProduct;
import com.example.finalproject.order.domain.StoreOrder;
import com.example.finalproject.order.repository.OrderProductRepository;
import com.example.finalproject.order.repository.StoreOrderRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderStatusService {

    private final OrderProductRepository orderProductRepository;
    private final StoreOrderRepository storeOrderRepository;

    @Transactional
    public void cancelAfterPayment(Long storeOrderId, String reason) {

        StoreOrder storeOrder = storeOrderRepository.findById(storeOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_ORDER_NOT_FOUND));

        Order order = storeOrder.getOrder();

        storeOrder.cancel(reason);

        List<OrderProduct> orderProducts = orderProductRepository.findAllByStoreOrderId(storeOrderId);
        for (OrderProduct op : orderProducts) {
            op.getProduct().increaseStock(op.getQuantity());
        }

        order.recalculateStatus();
    }
}
