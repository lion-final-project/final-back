package com.example.finalproject.order.service;

import com.example.finalproject.global.component.UserLoader;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.order.domain.StoreOrder;
import com.example.finalproject.order.repository.StoreOrderRepository;
import com.example.finalproject.payment.service.PaymentCancelService;
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
    private final PaymentCancelService paymentCancelService;
    private final StoreOrderStatusService storeOrderStatusService;

    public void cancelStoreOrder(String email, Long storeOrderId, String reason) {
        log.info("[STORE_ORDER_CANCEL_REQUEST] email={}, storeOrderId={}, reason={}", email, storeOrderId, reason);

        User user = userLoader.loadUserByUsername(email);

        StoreOrder storeOrder = storeOrderRepository.findById(storeOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_ORDER_NOT_FOUND));

        if (!storeOrder.getOrder().getUser().getId().equals(user.getId())) {
            log.warn("[STORE_ORDER_CANCEL_FORBIDDEN] email={}, storeOrderId={}", email, storeOrderId);
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        storeOrderStatusService.requestCancel(storeOrderId, reason);

        paymentCancelService.cancel(storeOrder, storeOrder.getFinalPrice(), reason);
    }
}
