package com.example.finalproject.order.service;

import com.example.finalproject.delivery.domain.Delivery;
import com.example.finalproject.delivery.repository.DeliveryRepository;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.order.domain.OrderProduct;
import com.example.finalproject.order.domain.StoreOrder;
import com.example.finalproject.order.dto.storeorder.request.PatchStoreOrderAcceptRequest;
import com.example.finalproject.order.dto.storeorder.response.GetCompletedStoreOrderResponse;
import com.example.finalproject.order.dto.storeorder.response.GetStoreOrderResponse;
import com.example.finalproject.order.enums.OrderStatus;
import com.example.finalproject.order.enums.StoreOrderStatus;
import com.example.finalproject.order.event.StoreOrderAcceptedEvent;
import com.example.finalproject.order.event.StoreOrderRejectedEvent;
import com.example.finalproject.order.repository.OrderProductRepository;
import com.example.finalproject.order.repository.StoreOrderRepository;
import com.example.finalproject.store.domain.Store;
import com.example.finalproject.store.enums.StoreActiveStatus;
import com.example.finalproject.store.repository.StoreRepository;
import com.example.finalproject.user.domain.User;

import com.example.finalproject.store.domain.StoreBusinessHour;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreOrderService {

    private final StoreOrderRepository storeOrderRepository;
    private final OrderProductRepository orderProductRepository;
    private final StoreRepository storeRepository;
    private final DeliveryRepository deliveryRepository;
    private final ApplicationEventPublisher eventPublisher;
    // TODO: 환불 관련 서비스


    public List<GetStoreOrderResponse> getNewOrders(String userEmail) {
        log.info("신규 주문 조회 시작 - userEmail={}", userEmail);
        Store store = getStoreByOwner(userEmail);
        List<StoreOrderStatus> statuses = List.of(StoreOrderStatus.PENDING, StoreOrderStatus.ACCEPTED, StoreOrderStatus.READY);

        List<StoreOrder> storeOrdersBeforeReady = storeOrderRepository.findByStoreIdAndStatusIn(store.getId(), statuses);
        List<OrderProduct> orderProducts = orderProductRepository.findByStoreOrderIn(storeOrdersBeforeReady);

        Map<Long, List<OrderProduct>> orderProductsByStoreOrderId = orderProducts.stream()
                .collect(Collectors.groupingBy(orderProduct -> orderProduct.getStoreOrder().getId()));

        List<GetStoreOrderResponse> result = storeOrdersBeforeReady.stream()
                .filter(storeOrder -> {
                    List<OrderProduct> products = orderProductsByStoreOrderId.get(storeOrder.getId());
                    if (products == null || products.isEmpty()) {
                        log.error("주문 상품 데이터 누락 - storeOrderId={}", storeOrder.getId());
                        return false;
                    }
                    return true;
                })
                .map(storeOrder -> GetStoreOrderResponse.from(storeOrder, orderProductsByStoreOrderId.get(storeOrder.getId())))
                .toList();
        log.info("신규 주문 조회 완료 - storeId={}, count={}", store.getId(), result.size());
        return result;
    }

    @Transactional
    public void acceptOrder(Long storeOrderId, PatchStoreOrderAcceptRequest request, String userEmail) {
        log.info("주문 접수 시작 - storeOrderId={}, userEmail={}", storeOrderId, userEmail);
        Store store = getStoreByOwner(userEmail);
        StoreOrder storeOrder = getStoreOrderWithOrderAndUser(storeOrderId);

        validateStoreOwnership(storeOrder, store);
        validateActiveStore(store);
        validateBusinessHour(store);

        OrderStatus orderStatus = storeOrder.getOrder().getStatus();
        validateOrderPaid(orderStatus);

        storeOrder.accept(request.getPrepTime());

        User customer = storeOrder.getOrder().getUser();

        //배달 생성
        Delivery delivery = new Delivery(storeOrder, storeOrder.getDeliveryFee());
        deliveryRepository.save(delivery);

        //접수 알림
        eventPublisher.publishEvent(new StoreOrderAcceptedEvent(customer.getId(), storeOrder.getOrder().getOrderNumber(), store.getStoreName()));
        log.info("주문 접수 완료 - storeOrderId={}, prepTime={}, deliveryId={}", storeOrderId, request.getPrepTime(), delivery.getId());

    }

    @Transactional
    public void rejectOrder(Long storeOrderId, String username, String reason) {
        log.info("주문 거절 시작 - storeOrderId={}, username={}", storeOrderId, username);
        Store store = getStoreByOwner(username);
        StoreOrder storeOrder = getStoreOrderWithOrderAndUser(storeOrderId);

        validateStoreOwnership(storeOrder, store);

        OrderStatus orderStatus = storeOrder.getOrder().getStatus();
        validateOrderPaid(orderStatus);

        storeOrder.reject(reason);

        User customer = storeOrder.getOrder().getUser();

        // 주문 거절 알림 발송
        eventPublisher.publishEvent(new StoreOrderRejectedEvent(customer.getId(), store.getStoreName()));

        // 신규 주문 현황 갱신
        log.info("주문 거절 완료 - storeOrderId={}, reason={}", storeOrderId, reason);

        // TODO: 환불 처리 구현
    }

    @Transactional
    public void completePreparation(Long storeOrderId, String username) {
        log.info("준비 완료 처리 시작 - storeOrderId={}, username={}", storeOrderId, username);
        Store store = getStoreByOwner(username);
        StoreOrder storeOrder = getStoreOrder(storeOrderId);

        validateStoreOwnership(storeOrder, store);
        validateActiveStore(store);
        validateBusinessHour(store);

        if (storeOrder.getStatus() != StoreOrderStatus.ACCEPTED) {
            throw new BusinessException(ErrorCode.STORE_ORDER_NOT_ACCEPTED);
        }

        storeOrder.markReady();
        log.info("준비 완료 처리 완료 - storeOrderId={}", storeOrderId);
    }

    public List<GetCompletedStoreOrderResponse> getCompletedOrders(String username) {
        log.info("완료 주문 조회 시작 - username={}", username);
        Store store = getStoreByOwner(username);
        List<StoreOrderStatus> statuses = List.of(
                StoreOrderStatus.PICKED_UP, StoreOrderStatus.DELIVERING
        );

        List<StoreOrder> completedOrders = storeOrderRepository.findCompletedByStoreIdAndStatusIn(store.getId(), statuses);
        List<OrderProduct> orderProducts = orderProductRepository.findByStoreOrderIn(completedOrders);

        Map<Long, List<OrderProduct>> orderProductsByStoreOrderId = orderProducts.stream()
                .collect(Collectors.groupingBy(op -> op.getStoreOrder().getId()));

        List<GetCompletedStoreOrderResponse> result = completedOrders.stream()
                .filter(storeOrder -> {
                    List<OrderProduct> products = orderProductsByStoreOrderId.get(storeOrder.getId());
                    if (products == null || products.isEmpty()) {
                        log.error("주문 상품 데이터 누락 - storeOrderId={}", storeOrder.getId());
                        return false;
                    }
                    return true;
                })
                .map(storeOrder -> GetCompletedStoreOrderResponse.from(storeOrder, orderProductsByStoreOrderId.get(storeOrder.getId())))
                .toList();
        log.info("완료 주문 조회 완료 - storeId={}, count={}", store.getId(), result.size());
        return result;
    }

    public Page<GetCompletedStoreOrderResponse> getAllOrders(String username, Pageable pageable) {
        Store store = getStoreByOwner(username);
        Page<StoreOrder> storeOrderPage = storeOrderRepository.findAllByStoreId(store.getId(), pageable);
        List<StoreOrder> storeOrders = storeOrderPage.getContent();

        List<OrderProduct> orderProducts = storeOrders.isEmpty()
                ? Collections.emptyList()
                : orderProductRepository.findByStoreOrderIn(storeOrders);

        Map<Long, List<OrderProduct>> orderProductsByStoreOrderId = orderProducts.stream()
                .collect(Collectors.groupingBy(op -> op.getStoreOrder().getId()));

        return storeOrderPage.map(storeOrder -> {
            List<OrderProduct> products = orderProductsByStoreOrderId.getOrDefault(storeOrder.getId(), Collections.emptyList());
            if (products.isEmpty()) {
                log.error("주문 상품 데이터 누락 - storeOrderId={}", storeOrder.getId());
            }
            return GetCompletedStoreOrderResponse.from(storeOrder, products);
        });
    }

    private Store getStoreByOwner(String userEmail) {
        return storeRepository.findByOwnerEmail(userEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));
    }

    private StoreOrder getStoreOrder(Long storeOrderId) {
        return storeOrderRepository.findById(storeOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_ORDER_NOT_FOUND));
    }

    private StoreOrder getStoreOrderWithOrderAndUser(Long storeOrderId) {
        return storeOrderRepository.findByIdWithOrderAndUser(storeOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_ORDER_NOT_FOUND));
    }

    private void validateOrderPaid(OrderStatus orderStatus) {
        if (orderStatus != OrderStatus.PAID) {
            throw new BusinessException(ErrorCode.ORDER_NOT_PAID);
        }
    }

    private void validateStoreOwnership(StoreOrder storeOrder, Store store) {
        if (!storeOrder.getStore().getId().equals(store.getId())) {
            throw new BusinessException(ErrorCode.STORE_ORDER_FORBIDDEN);
        }
    }

    private void validateActiveStore(Store store) {
        if (store.getIsActive() != StoreActiveStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.STORE_NOT_ACTIVE);
        }
    }

    private void validateBusinessHour(Store store) {
        LocalDateTime now = LocalDateTime.now();
        short dayOfWeek = (short) now.getDayOfWeek().getValue();
        LocalTime currentTime = now.toLocalTime();

        StoreBusinessHour businessHour = store.getBusinessHours().stream()
                .filter(bh -> bh.getDayOfWeek().equals(dayOfWeek)).findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_OUTSIDE_BUSINESS_HOURS));

        if (businessHour.getIsClosed()) {
            throw new BusinessException(ErrorCode.STORE_OUTSIDE_BUSINESS_HOURS);
        }

        if (currentTime.isBefore(businessHour.getOpenTime()) || currentTime.isAfter(businessHour.getCloseTime())) {
            throw new BusinessException(ErrorCode.STORE_OUTSIDE_BUSINESS_HOURS);
        }
    }


}
