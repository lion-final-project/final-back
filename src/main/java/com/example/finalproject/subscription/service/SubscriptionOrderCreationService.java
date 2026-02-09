package com.example.finalproject.subscription.service;

import com.example.finalproject.order.domain.Order;
import com.example.finalproject.order.domain.OrderProduct;
import com.example.finalproject.order.domain.StoreOrder;
import com.example.finalproject.order.enums.OrderType;
import com.example.finalproject.order.repository.OrderProductRepository;
import com.example.finalproject.order.repository.OrderRepository;
import com.example.finalproject.order.repository.StoreOrderRepository;
import com.example.finalproject.product.domain.Product;
import com.example.finalproject.subscription.domain.Subscription;
import com.example.finalproject.subscription.domain.SubscriptionHistory;
import com.example.finalproject.subscription.domain.SubscriptionProductItem;
import com.example.finalproject.subscription.enums.SubHistoryStatus;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.subscription.repository.SubscriptionHistoryRepository;
import com.example.finalproject.subscription.repository.SubscriptionProductItemRepository;
import com.example.finalproject.user.domain.Address;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 구독 배송 일정에 따라 Order → StoreOrder → OrderProduct 를 자동 생성하고
 * subscription_history 에 연결한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionOrderCreationService {

    private static final DateTimeFormatter ORDER_NUMBER_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final SubscriptionHistoryRepository subscriptionHistoryRepository;
    private final SubscriptionProductItemRepository subscriptionProductItemRepository;
    private final OrderRepository orderRepository;
    private final StoreOrderRepository storeOrderRepository;
    private final OrderProductRepository orderProductRepository;

    /**
     * 오늘 예정된 SCHEDULED 구독 이력을 주문으로 생성한다.
     * 매일 새벽(스케줄러)에서 호출한다.
     *
     * @param scheduledDate 처리할 배송 예정일 (보통 오늘)
     * @return 생성된 주문(StoreOrder) 건수
     */
    @Transactional
    public int createOrdersForScheduledDate(LocalDate scheduledDate) {
        List<SubscriptionHistory> scheduled = subscriptionHistoryRepository
                .findByStatusAndScheduledDate(SubHistoryStatus.SCHEDULED, scheduledDate);
        if (scheduled.isEmpty()) {
            log.debug("구독 주문 생성: scheduledDate={}, 건수=0", scheduledDate);
            return 0;
        }

        int created = 0;
        for (SubscriptionHistory history : scheduled) {
            try {
                createOrderFromHistory(history);
                created++;
            } catch (Exception e) {
                log.warn("구독 주문 생성 실패 historyId={}, subscriptionId={}", history.getId(),
                        history.getSubscription().getId(), e);
            }
        }
        log.info("구독 주문 생성 완료: scheduledDate={}, 생성 건수={}/{}", scheduledDate, created, scheduled.size());
        return created;
    }

    private void createOrderFromHistory(SubscriptionHistory history) {
        Subscription sub = history.getSubscription();
        Address address = sub.getAddress();

        String orderNumber = "SUB" + history.getScheduledDate().format(ORDER_NUMBER_DATE) + "-" + history.getId();
        String deliveryAddress = buildDeliveryAddress(address);
        LocalDateTime orderedAt = LocalDateTime.now();

        List<SubscriptionProductItem> items = subscriptionProductItemRepository
                .findBySubscriptionProductOrderById(sub.getSubscriptionProduct());
        if (items.isEmpty()) {
            throw new BusinessException(ErrorCode.SUBSCRIPTION_PRODUCT_HAS_NO_ITEMS);
        }

        int totalProductPrice = 0;
        for (SubscriptionProductItem item : items) {
            Product product = item.getProduct();
            int qty = item.getQuantity() != null ? item.getQuantity() : 1;
            int priceSnapshot = product.getSalePrice() != null ? product.getSalePrice() : product.getPrice();
            totalProductPrice += priceSnapshot * qty;
        }
        int totalDeliveryFee = 0;
        int finalPrice = totalProductPrice + totalDeliveryFee;

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(sub.getUser())
                .orderType(OrderType.SUBSCRIPTION)
                .totalProductPrice(totalProductPrice)
                .totalDeliveryFee(totalDeliveryFee)
                .finalPrice(finalPrice)
                .deliveryAddress(deliveryAddress)
                .deliveryLocation(address.getLocation())
                .orderedAt(orderedAt)
                .build();
        orderRepository.save(order);

        StoreOrder storeOrder = storeOrderRepository.save(
                StoreOrder.builder()
                        .order(order)
                        .store(sub.getStore())
                        .orderType(OrderType.SUBSCRIPTION)
                        .storeProductPrice(totalProductPrice)
                        .deliveryFee(totalDeliveryFee)
                        .finalPrice(finalPrice)
                        .build());

        for (SubscriptionProductItem item : items) {
            Product product = item.getProduct();
            int qty = item.getQuantity() != null ? item.getQuantity() : 1;
            int priceSnapshot = product.getSalePrice() != null ? product.getSalePrice() : product.getPrice();
            orderProductRepository.save(OrderProduct.builder()
                    .storeOrder(storeOrder)
                    .product(product)
                    .productNameSnapshot(product.getProductName())
                    .priceSnapshot(priceSnapshot)
                    .quantity(qty)
                    .build());
        }

        history.linkStoreOrder(storeOrder);
        subscriptionHistoryRepository.save(history);
    }

    private static String buildDeliveryAddress(Address address) {
        String line2 = address.getAddressLine2();
        String second = (line2 != null && !line2.isBlank()) ? " " + line2 : "";
        return address.getAddressLine1() + second;
    }
}
