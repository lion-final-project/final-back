package com.example.finalproject.admin.service.finance;

import com.example.finalproject.admin.dto.finance.payment.AdminPaymentListResponse;
import com.example.finalproject.admin.dto.finance.payment.AdminPaymentSummaryResponse;
import com.example.finalproject.order.domain.StoreOrder;
import com.example.finalproject.order.enums.OrderType;
import com.example.finalproject.order.repository.StoreOrderRepository;
import com.example.finalproject.payment.domain.Payment;
import com.example.finalproject.payment.enums.PaymentStatus;
import com.example.finalproject.payment.enums.RefundStatus;
import com.example.finalproject.payment.repository.PaymentRefundRepository;
import com.example.finalproject.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminFinancePaymentService {

    private static final double PLATFORM_FEE_RATE = 0.05;
    private static final Set<PaymentStatus> COUNTABLE_PAYMENT_STATUSES = Set.of(
            PaymentStatus.APPROVED, PaymentStatus.PARTIAL_REFUNDED, PaymentStatus.REFUNDED, PaymentStatus.REFUND_REQUESTED
    );

    private final AdminFinanceCommonSupport common;
    private final StoreOrderRepository storeOrderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentRefundRepository paymentRefundRepository;

    public AdminPaymentSummaryResponse getPaymentSummary(String adminEmail, String yearMonth) {
        common.validateAdmin(adminEmail);
        AdminFinanceCommonSupport.DateRange range = common.resolveMonthRange(yearMonth);
        AdminFinanceCommonSupport.SummaryData summaryData = buildPaymentSummary(range.startDateTime(), range.endExclusiveDateTime());

        long regularSalesAmount = storeOrderRepository.sumFinalPriceByOrderTypeAndOrderedAtBetween(
                OrderType.REGULAR, range.startDateTime(), range.endExclusiveDateTime()
        );
        long subscriptionSalesAmount = storeOrderRepository.sumFinalPriceByOrderTypeAndOrderedAtBetween(
                OrderType.SUBSCRIPTION, range.startDateTime(), range.endExclusiveDateTime()
        );

        return AdminPaymentSummaryResponse.builder()
                .grossPaymentAmount(summaryData.totalAmount())
                .platformFeeRevenue(summaryData.totalCommission())
                .refundAmount(summaryData.totalRefundAmount())
                .netRevenue(summaryData.netRevenue())
                .paymentCount(summaryData.paymentCount())
                .refundRequestedCount(paymentRefundRepository.countByRefundStatus(RefundStatus.REQUESTED))
                .refundApprovedCount(paymentRefundRepository.countByRefundStatus(RefundStatus.APPROVED))
                .refundRejectedCount(paymentRefundRepository.countByRefundStatus(RefundStatus.REJECTED))
                .refundRequestedAmount(paymentRefundRepository.sumRefundAmountByRefundStatusAndRefundedAtBetween(
                        RefundStatus.REQUESTED, range.startDateTime(), range.endExclusiveDateTime()
                ))
                .refundApprovedAmount(paymentRefundRepository.sumRefundAmountByRefundStatusAndRefundedAtBetween(
                        RefundStatus.APPROVED, range.startDateTime(), range.endExclusiveDateTime()
                ))
                .refundRejectedAmount(paymentRefundRepository.sumRefundAmountByRefundStatusAndRefundedAtBetween(
                        RefundStatus.REJECTED, range.startDateTime(), range.endExclusiveDateTime()
                ))
                .regularSalesAmount(regularSalesAmount)
                .subscriptionSalesAmount(subscriptionSalesAmount)
                .build();
    }

    public AdminPaymentListResponse getPayments(String adminEmail, String yearMonth, String keyword, Pageable pageable) {
        common.validateAdmin(adminEmail);
        AdminFinanceCommonSupport.DateRange range = common.resolveMonthRange(yearMonth);
        String normalizedKeyword = common.normalizeKeyword(keyword);

        Page<StoreOrder> storeOrderPage = storeOrderRepository.searchForAdminPayments(
                range.startDateTime(), range.endExclusiveDateTime(), normalizedKeyword, pageable
        );

        List<Long> orderIds = storeOrderPage.getContent().stream()
                .map(storeOrder -> storeOrder.getOrder().getId())
                .distinct()
                .toList();
        List<Long> storeOrderIds = storeOrderPage.getContent().stream().map(StoreOrder::getId).toList();

        Map<Long, Payment> paymentByOrderId = paymentRepository.findByOrder_IdIn(orderIds).stream()
                .collect(Collectors.toMap(payment -> payment.getOrder().getId(), payment -> payment, (left, right) -> left));

        Map<Long, Long> refundByStoreOrderId = new HashMap<>();
        if (!storeOrderIds.isEmpty()) {
            for (Object[] row : paymentRefundRepository.sumRefundAmountGroupByStoreOrderId(storeOrderIds)) {
                Long storeOrderId = ((Number) row[0]).longValue();
                Long refundAmount = ((Number) row[1]).longValue();
                refundByStoreOrderId.put(storeOrderId, refundAmount);
            }
        }

        List<AdminPaymentListResponse.Item> content = new ArrayList<>();
        for (StoreOrder storeOrder : storeOrderPage.getContent()) {
            Payment payment = paymentByOrderId.get(storeOrder.getOrder().getId());
            long amount = storeOrder.getFinalPrice() == null ? 0L : storeOrder.getFinalPrice();
            long refundAmount = refundByStoreOrderId.getOrDefault(storeOrder.getId(), 0L);
            long effectiveAmount = Math.max(0L, amount - refundAmount);
            long commission = Math.round(effectiveAmount * PLATFORM_FEE_RATE);

            content.add(AdminPaymentListResponse.Item.builder()
                    .storeOrderId(storeOrder.getId())
                    .orderNumber(storeOrder.getOrder().getOrderNumber())
                    .mart(storeOrder.getStore().getStoreName())
                    .category(storeOrder.getStore().getStoreCategory() != null
                            ? storeOrder.getStore().getStoreCategory().getCategoryName()
                            : "미분류")
                    .region(common.extractRegion(storeOrder.getStore()))
                    .customerName(storeOrder.getOrder().getUser().getName())
                    .amount(amount)
                    .commission(commission)
                    .refundAmount(refundAmount)
                    .status(common.toPaymentStatusLabel(payment))
                    .paymentStatus(payment != null && payment.getPaymentStatus() != null ? payment.getPaymentStatus().name() : null)
                    .paymentMethod(payment != null && payment.getPaymentMethod() != null ? payment.getPaymentMethod().name() : null)
                    .paidAt(payment != null ? payment.getPaidAt() : null)
                    .orderedAt(storeOrder.getOrder().getOrderedAt())
                    .build());
        }

        AdminFinanceCommonSupport.SummaryData summaryData = buildPaymentSummary(range.startDateTime(), range.endExclusiveDateTime());
        return AdminPaymentListResponse.builder()
                .content(content)
                .stats(AdminPaymentListResponse.Stats.builder()
                        .totalAmount(summaryData.totalAmount())
                        .totalCommission(summaryData.totalCommission())
                        .totalRefundAmount(summaryData.totalRefundAmount())
                        .netRevenue(summaryData.netRevenue())
                        .build())
                .page(AdminPaymentListResponse.PageInfo.builder()
                        .page(storeOrderPage.getNumber())
                        .size(storeOrderPage.getSize())
                        .totalElements(storeOrderPage.getTotalElements())
                        .totalPages(storeOrderPage.getTotalPages())
                        .hasNext(storeOrderPage.hasNext())
                        .build())
                .build();
    }

    private AdminFinanceCommonSupport.SummaryData buildPaymentSummary(LocalDateTime start, LocalDateTime endExclusive) {
        long totalAmount = storeOrderRepository.sumFinalPriceByOrderOrderedAtBetween(start, endExclusive);
        long totalCommission = Math.round(totalAmount * PLATFORM_FEE_RATE);
        long totalRefundAmount = paymentRepository.sumRefundedAmountByPaidAtBetween(start, endExclusive);
        long netRevenue = Math.max(0L, totalAmount - totalCommission - totalRefundAmount);
        long paymentCount = paymentRepository.countByPaymentStatusInAndPaidAtBetween(COUNTABLE_PAYMENT_STATUSES, start, endExclusive);
        return new AdminFinanceCommonSupport.SummaryData(totalAmount, totalCommission, totalRefundAmount, netRevenue, paymentCount);
    }
}
