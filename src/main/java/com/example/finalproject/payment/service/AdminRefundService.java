package com.example.finalproject.payment.service;

import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.payment.domain.PaymentRefund;
import com.example.finalproject.payment.dto.request.PostPaymentRefundApproveRequest;
import com.example.finalproject.payment.repository.PaymentRefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminRefundService {

    private final AdminRefundCommandService commandService;
    private final PaymentRefundRepository refundRepository;
    private final PaymentCancelService paymentCancelService;

    public void approveAndCancel(Long refundId, PostPaymentRefundApproveRequest req) {

        commandService.approve(refundId, req);

        PaymentRefund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFUND_NOT_FOUND));

        paymentCancelService.cancel(
                refund.getStoreOrder(),
                refund.getRefundAmount(),
                refund.getRefundReason()
        );
    }
}