package com.example.finalproject.order.service;

import com.example.finalproject.global.component.UserLoader;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.order.domain.Order;
import com.example.finalproject.order.domain.StoreOrder;
import com.example.finalproject.order.repository.StoreOrderRepository;
import com.example.finalproject.payment.service.PaymentService;
import com.example.finalproject.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class StoreOrderCancelService {
    private final UserLoader userLoader;
    private final StoreOrderRepository storeOrderRepository;
    private final PaymentService paymentService;

    public void cancelStoreOrder(String email, Long storeOrderId, String reason) {
        User user = userLoader.loadUserByUsername(email);

        StoreOrder storeOrder = storeOrderRepository.findById(storeOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_ORDER_NOT_FOUND));

        Order order = storeOrder.getOrder();

        if (!order.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        storeOrder.validateCancelable();

        storeOrder.markCancelCancelRequested();

        paymentService.cancelPayment(storeOrder, storeOrder.getFinalPrice(), reason);
    }
}
