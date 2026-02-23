package com.example.finalproject.admin.service.finance;

import com.example.finalproject.admin.dto.finance.overview.AdminOverviewStatsResponse;
import com.example.finalproject.admin.dto.finance.transaction.AdminTransactionDetailResponse;
import com.example.finalproject.admin.dto.finance.transaction.AdminTransactionOrderDetailResponse;
import com.example.finalproject.admin.dto.finance.transaction.AdminTransactionTrendResponse;
import com.example.finalproject.communication.enums.InquiryStatus;
import com.example.finalproject.communication.repository.InquiryRepository;
import com.example.finalproject.delivery.domain.Delivery;
import com.example.finalproject.delivery.enums.DeliveryStatus;
import com.example.finalproject.delivery.repository.DeliveryRepository;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.moderation.enums.ReportStatus;
import com.example.finalproject.moderation.repository.ReportRepository;
import com.example.finalproject.order.domain.OrderLine;
import com.example.finalproject.order.domain.OrderProduct;
import com.example.finalproject.order.domain.StoreOrder;
import com.example.finalproject.order.repository.OrderLineRepository;
import com.example.finalproject.order.repository.OrderProductRepository;
import com.example.finalproject.order.repository.StoreOrderRepository;
import com.example.finalproject.payment.domain.Payment;
import com.example.finalproject.payment.domain.PaymentRefund;
import com.example.finalproject.payment.repository.PaymentRefundRepository;
import com.example.finalproject.payment.repository.PaymentRepository;
import com.example.finalproject.settlement.enums.SettlementStatus;
import com.example.finalproject.settlement.enums.SettlementTargetType;
import com.example.finalproject.settlement.store.repository.SettlementRepository;
import com.example.finalproject.store.repository.StoreRepository;
import com.example.finalproject.store.enums.StoreStatus;
import com.example.finalproject.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminFinanceOverviewService {

    private final AdminFinanceCommonSupport common;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final ReportRepository reportRepository;
    private final InquiryRepository inquiryRepository;
    private final DeliveryRepository deliveryRepository;
    private final StoreOrderRepository storeOrderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentRefundRepository paymentRefundRepository;
    private final OrderProductRepository orderProductRepository;
    private final OrderLineRepository orderLineRepository;
    private final SettlementRepository settlementRepository;

    public AdminOverviewStatsResponse getOverviewStats(String adminEmail) {
        common.validateAdmin(adminEmail);
        return AdminOverviewStatsResponse.builder()
                .totalUsers(userRepository.countByDeletedAtIsNull())
                .approvedStores(storeRepository.countByStatus(StoreStatus.APPROVED))
                .deliveringRiders(deliveryRepository.countDistinctRiderByStatus(DeliveryStatus.DELIVERING))
                .pendingStoreSettlements(settlementRepository.countByTargetTypeAndStatus(SettlementTargetType.STORE, SettlementStatus.PENDING))
                .pendingReports(reportRepository.countByStatus(ReportStatus.PENDING))
                .pendingInquiries(inquiryRepository.countByStatus(InquiryStatus.PENDING))
                .build();
    }

    public AdminTransactionTrendResponse getTransactionTrend(String adminEmail, String period) {
        common.validateAdmin(adminEmail);
        String normalizedPeriod = common.normalizePeriod(period);
        LocalDateTime baseDateTime = resolveTrendBaseDateTime();
        LocalDateTime end = baseDateTime;
        LocalDateTime start;

        List<String> labels = new ArrayList<>();
        Map<String, Long> amountByLabel = new LinkedHashMap<>();
        if ("weekly".equals(normalizedPeriod)) {
            start = baseDateTime.toLocalDate().minusDays(6).atStartOfDay();
            for (int i = 0; i < 7; i++) {
                LocalDate day = start.toLocalDate().plusDays(i);
                String label = day.getMonthValue() + "/" + day.getDayOfMonth();
                labels.add(label);
                amountByLabel.put(label, 0L);
            }
        } else if ("monthly".equals(normalizedPeriod)) {
            LocalDate monthStart = baseDateTime.toLocalDate().withDayOfMonth(1);
            start = monthStart.atStartOfDay();
            int daysInMonth = monthStart.lengthOfMonth();
            for (int day = 1; day <= daysInMonth; day++) {
                String label = day + "일";
                labels.add(label);
                amountByLabel.put(label, 0L);
            }
        } else {
            LocalDate yearStart = baseDateTime.toLocalDate().withDayOfYear(1);
            start = yearStart.atStartOfDay();
            for (int month = 1; month <= 12; month++) {
                String label = month + "월";
                labels.add(label);
                amountByLabel.put(label, 0L);
            }
        }

        List<StoreOrder> orders = storeOrderRepository.findByOrder_OrderedAtBetween(start, end);
        Map<Long, Long> refundByStoreOrderId = buildRefundMap(orders);
        for (StoreOrder storeOrder : orders) {
            if (storeOrder.getOrder() == null || storeOrder.getOrder().getOrderedAt() == null) {
                continue;
            }
            LocalDateTime orderedAt = storeOrder.getOrder().getOrderedAt();
            String key = switch (normalizedPeriod) {
                case "monthly" -> orderedAt.getDayOfMonth() + "일";
                case "yearly" -> orderedAt.getMonthValue() + "월";
                default -> orderedAt.getMonthValue() + "/" + orderedAt.getDayOfMonth();
            };
            long grossAmount = storeOrder.getFinalPrice() == null ? 0L : storeOrder.getFinalPrice();
            long refundAmount = refundByStoreOrderId.getOrDefault(storeOrder.getId(), 0L);
            long netAmount = Math.max(0L, grossAmount - refundAmount);
            amountByLabel.computeIfPresent(key, (k, v) -> v + netAmount);
        }

        List<Long> yValues = labels.stream().map(label -> amountByLabel.getOrDefault(label, 0L)).toList();
        long maxY = yValues.stream().mapToLong(Long::longValue).max().orElse(0L);
        return AdminTransactionTrendResponse.builder()
                .period(normalizedPeriod)
                .xLabels(labels)
                .yValues(yValues)
                .maxY(maxY)
                .build();
    }

    public AdminTransactionDetailResponse getTransactionDetail(String adminEmail, String period, String label) {
        common.validateAdmin(adminEmail);
        String normalizedPeriod = common.normalizePeriod(period);
        LocalDateTime baseDateTime = resolveTrendBaseDateTime();

        LocalDateTime bucketStart;
        LocalDateTime bucketEnd;
        if ("weekly".equals(normalizedPeriod)) {
            LocalDate weeklyStart = baseDateTime.toLocalDate().minusDays(6);
            bucketStart = null;
            for (int i = 0; i < 7; i++) {
                LocalDate day = weeklyStart.plusDays(i);
                String dayLabel = day.getMonthValue() + "/" + day.getDayOfMonth();
                if (dayLabel.equals(label)) {
                    bucketStart = day.atStartOfDay();
                    break;
                }
            }
            if (bucketStart == null) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "유효하지 않은 날짜 라벨입니다.");
            }
            bucketEnd = bucketStart.plusDays(1);
        } else if ("monthly".equals(normalizedPeriod)) {
            LocalDate monthStart = baseDateTime.toLocalDate().withDayOfMonth(1);
            String numeric = label.replaceAll("[^0-9]", "");
            if (numeric.isBlank()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "유효하지 않은 일자 라벨입니다.");
            }
            int day = Integer.parseInt(numeric);
            if (day < 1 || day > monthStart.lengthOfMonth()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "유효하지 않은 일자 라벨입니다.");
            }
            bucketStart = monthStart.withDayOfMonth(day).atStartOfDay();
            bucketEnd = bucketStart.plusDays(1);
        } else {
            String numeric = label.replaceAll("[^0-9]", "");
            if (numeric.isBlank()) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "유효하지 않은 월 라벨입니다.");
            }
            int month = Integer.parseInt(numeric);
            if (month < 1 || month > 12) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "유효하지 않은 월 라벨입니다.");
            }
            LocalDate monthDate = LocalDate.of(baseDateTime.getYear(), month, 1);
            bucketStart = monthDate.atStartOfDay();
            bucketEnd = monthDate.plusMonths(1).atStartOfDay();
        }

        List<StoreOrder> orders = storeOrderRepository.findByOrder_OrderedAtBetween(bucketStart, bucketEnd).stream()
                .filter(so -> so.getOrder() != null && so.getOrder().getOrderedAt() != null)
                .sorted((a, b) -> b.getOrder().getOrderedAt().compareTo(a.getOrder().getOrderedAt()))
                .toList();

        Map<Long, Long> refundByStoreOrderId = buildRefundMap(orders);
        List<AdminTransactionDetailResponse.Item> content = orders.stream().map(storeOrder ->
                AdminTransactionDetailResponse.Item.builder()
                        .storeOrderId(storeOrder.getId())
                        .orderNumber(storeOrder.getOrder() != null ? storeOrder.getOrder().getOrderNumber() : "-")
                        .storeName(storeOrder.getStore() != null ? storeOrder.getStore().getStoreName() : "-")
                        .customerName(storeOrder.getOrder() != null && storeOrder.getOrder().getUser() != null
                                ? storeOrder.getOrder().getUser().getName() : "-")
                        .amount(Math.max(0L, (storeOrder.getFinalPrice() == null ? 0L : storeOrder.getFinalPrice())
                                - refundByStoreOrderId.getOrDefault(storeOrder.getId(), 0L)))
                        .orderStatus(storeOrder.getStatus() != null ? storeOrder.getStatus().name() : "-")
                        .orderedAt(storeOrder.getOrder() != null ? storeOrder.getOrder().getOrderedAt() : null)
                        .build()
        ).toList();

        long totalAmount = content.stream().mapToLong(AdminTransactionDetailResponse.Item::getAmount).sum();
        return AdminTransactionDetailResponse.builder()
                .period(normalizedPeriod)
                .label(label)
                .rangeStart(bucketStart)
                .rangeEnd(bucketEnd.minusNanos(1))
                .totalCount(content.size())
                .totalAmount(totalAmount)
                .content(content)
                .build();
    }

    public AdminTransactionOrderDetailResponse getTransactionOrderDetail(String adminEmail, Long storeOrderId) {
        common.validateAdmin(adminEmail);
        StoreOrder storeOrder = storeOrderRepository.findById(storeOrderId).orElse(null);
        if (storeOrder == null) {
            List<StoreOrder> byOrderId = storeOrderRepository.findAllByOrderId(storeOrderId);
            storeOrder = byOrderId.isEmpty() ? null : byOrderId.get(0);
        }
        if (storeOrder == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "주문 상세 정보를 찾을 수 없습니다.");
        }

        Long resolvedStoreOrderId = storeOrder.getId();
        List<OrderProduct> products = orderProductRepository.findAllByStoreOrderId(resolvedStoreOrderId);
        List<AdminTransactionOrderDetailResponse.ProductItem> productItems = products.stream()
                .map(item -> AdminTransactionOrderDetailResponse.ProductItem.builder()
                        .name(item.getProductNameSnapshot())
                        .unitPrice(item.getPriceSnapshot())
                        .quantity(item.getQuantity())
                        .build())
                .toList();

        if (productItems.isEmpty()) {
            Long orderId = storeOrder.getOrder().getId();
            Long storeId = storeOrder.getStore().getId();
            productItems = orderLineRepository.findAllByOrderId(orderId).stream()
                    .filter(line -> Objects.equals(line.getStoreId(), storeId))
                    .map(line -> AdminTransactionOrderDetailResponse.ProductItem.builder()
                            .name(line.getProductNameSnapshot())
                            .unitPrice(line.getPriceSnapshot())
                            .quantity(line.getQuantity())
                            .build())
                    .toList();
        }

        Delivery delivery = deliveryRepository.findByStoreOrderId(resolvedStoreOrderId).orElse(null);
        Payment payment = paymentRepository.findByOrder_Id(storeOrder.getOrder().getId()).orElse(null);
        List<PaymentRefund> refunds = paymentRefundRepository.findByStoreOrderIdOrderByCreatedAtDesc(resolvedStoreOrderId);
        long refundAmount = refunds.stream()
                .map(PaymentRefund::getRefundAmount)
                .filter(Objects::nonNull)
                .mapToLong(Integer::longValue)
                .sum();
        String refundStatus = refunds.isEmpty() ? "NONE" : refunds.get(0).getRefundStatus().name();

        String deliveryLocation = "-";
        if (delivery != null && delivery.getCustomerLocation() != null) {
            deliveryLocation = String.format(Locale.KOREA, "%.5f, %.5f",
                    delivery.getCustomerLocation().getY(), delivery.getCustomerLocation().getX());
        }

        String deliveryAddress = storeOrder.getOrder().getDeliveryAddress();
        if (deliveryAddress == null || deliveryAddress.isBlank()) {
            deliveryAddress = "주소 정보 없음";
        }

        String paymentStatus = payment != null && payment.getPaymentStatus() != null
                ? payment.getPaymentStatus().name() : "PENDING";

        return AdminTransactionOrderDetailResponse.builder()
                .storeOrderId(resolvedStoreOrderId)
                .orderNumber(storeOrder.getOrder().getOrderNumber())
                .storeName(storeOrder.getStore() != null ? storeOrder.getStore().getStoreName() : "-")
                .customerName(storeOrder.getOrder().getUser() != null ? storeOrder.getOrder().getUser().getName() : "-")
                .riderName(delivery != null && delivery.getRider() != null ? delivery.getRider().getDisplayName() : "-")
                .riderPhone(delivery != null && delivery.getRider() != null ? delivery.getRider().getDisplayPhone() : "-")
                .deliveryFee(storeOrder.getDeliveryFee())
                .deliveryAddress(deliveryAddress)
                .deliveryLocation(deliveryLocation)
                .orderedAt(storeOrder.getOrder().getOrderedAt())
                .paymentStatus(paymentStatus)
                .refundStatus(refundStatus)
                .refundAmount(refundAmount)
                .products(productItems)
                .build();
    }

    private LocalDateTime resolveTrendBaseDateTime() {
        LocalDateTime maxOrderedAt = storeOrderRepository.findMaxOrderOrderedAt();
        return maxOrderedAt != null ? maxOrderedAt : LocalDateTime.now();
    }

    private Map<Long, Long> buildRefundMap(List<StoreOrder> orders) {
        if (orders == null || orders.isEmpty()) {
            return Map.of();
        }
        List<Long> storeOrderIds = orders.stream().map(StoreOrder::getId).toList();
        Map<Long, Long> refundByStoreOrderId = new HashMap<>();
        for (Object[] row : paymentRefundRepository.sumRefundAmountGroupByStoreOrderId(storeOrderIds)) {
            Long soId = ((Number) row[0]).longValue();
            Long refundAmount = ((Number) row[1]).longValue();
            refundByStoreOrderId.put(soId, refundAmount);
        }
        return refundByStoreOrderId;
    }
}
