package com.example.finalproject.payment.service;

import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.order.domain.StoreOrder;
import com.example.finalproject.order.repository.StoreOrderRepository;
import com.example.finalproject.order.service.OrderStatusService;
import com.example.finalproject.payment.domain.Payment;
import com.example.finalproject.payment.domain.PaymentRefund;
import com.example.finalproject.payment.enums.PaymentStatus;
import com.example.finalproject.payment.repository.PaymentRefundRepository;
import com.example.finalproject.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentTxService {

    private final PaymentRepository paymentRepository;
    private final PaymentRefundRepository paymentRefundRepository;
    private final StoreOrderRepository storeOrderRepository;
    private final OrderStatusService orderStatusService;

    @Transactional
    public void applyRefund(Long orderId,
                            Long storeOrderId,
                            Integer cancelAmount,
                            String reason,
                            Integer pgCumulativeAmount) {

        Payment payment = paymentRepository.findWithLockByOrder_Id(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getPaymentStatus() != PaymentStatus.CANCEL_REQUESTED) {
            throw new BusinessException(ErrorCode.INVALID_PAYMENT_CANCEL_STATUS);
        }

        StoreOrder storeOrder = storeOrderRepository.findById(storeOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_ORDER_NOT_FOUND));

        paymentRefundRepository.save(
                PaymentRefund.builder()
                        .payment(payment)
                        .storeOrder(storeOrder)
                        .refundAmount(cancelAmount)
                        .refundReason(reason)
                        .build()
        );

        if (pgCumulativeAmount > payment.getAmount()) {
            throw new BusinessException(ErrorCode.INVALID_REFUND_AMOUNT);
        }

        if (pgCumulativeAmount.equals(payment.getAmount())) {
            payment.cancel();
        } else {
            payment.partialCancel(pgCumulativeAmount);
        }

        orderStatusService.cancelAfterPayment(storeOrderId, reason);
    }

    @Transactional
    public void markCancelRequested(Long orderId) {

        Payment payment = paymentRepository.findWithLockByOrder_Id(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getPaymentStatus() == PaymentStatus.CANCEL_REQUESTED) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_PAYMENT);
        }

        if (payment.isFullyRefunded()) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_PAYMENT);
        }

        payment.markCancelRequested();
    }

    @Transactional
    public void revertCancelRequest(Long orderId) {

        Payment payment = paymentRepository.findWithLockByOrder_Id(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getPaymentStatus() == PaymentStatus.CANCEL_REQUESTED) {
            payment.revertToPaid();
        }
    }

}



