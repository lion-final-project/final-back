package com.example.finalproject.order.service;

import com.example.finalproject.global.component.UserLoader;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.order.domain.Order;
import com.example.finalproject.order.domain.OrderProduct;
import com.example.finalproject.order.domain.StoreOrder;
import com.example.finalproject.order.dto.response.GetOrderDetailResponse;
import com.example.finalproject.order.dto.response.GetOrderListResponse;
import com.example.finalproject.order.dto.response.GetStoreOrderDetailResponse;
import com.example.finalproject.order.mapper.OrderDetailMapper;
import com.example.finalproject.order.mapper.OrderListMapper;
import com.example.finalproject.order.mapper.StoreOrderDetailMapper;
import com.example.finalproject.order.repository.OrderProductRepository;
import com.example.finalproject.order.repository.OrderRepository;
import com.example.finalproject.order.repository.StoreOrderRepository;
import com.example.finalproject.payment.domain.Payment;
import com.example.finalproject.payment.repository.PaymentRepository;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderQueryService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final StoreOrderRepository storeOrderRepository;
    private final OrderProductRepository orderProductRepository;
    private final PaymentRepository paymentRepository;
    private final UserLoader userLoader;

    private final OrderDetailMapper orderDetailMapper;
    private final StoreOrderDetailMapper storeOrderDetailMapper;
    private final OrderListMapper orderListMapper;

    // 주문 상세 조회
    public GetOrderDetailResponse getOrderDetail(String email, Long orderId) {
        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 주문 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        // 주문 소유자 확인
        if (!order.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 주문 상세 조회
        List<StoreOrder> storeOrders = storeOrderRepository.findAllByOrderId(orderId);
        List<OrderProduct> orderProducts = orderProductRepository.findAllByStoreOrderOrderId(orderId);

        // 주문 상세 조회
        Map<Long, List<OrderProduct>> productsByStoreOrder = orderProducts.stream()
                .collect(Collectors.groupingBy(op -> op.getStoreOrder().getId()));

        // 주문 상세 조회
        List<GetOrderDetailResponse.StoreOrderInfo> storeOrderInfos = storeOrders.stream()
                .map(storeOrder -> {
                    // 주문 상세 조회
                    List<GetOrderDetailResponse.ProductInfo> products = productsByStoreOrder
                            .getOrDefault(storeOrder.getId(), List.of())
                            .stream()
                            .map(orderDetailMapper::toProductInfo)
                            .toList();

                    // 주문 상세 조회
                    return orderDetailMapper.toStoreOrderInfo(storeOrder, products);
                })
                .toList();

        // 결제 조회
        Payment payment = paymentRepository.findByOrder_Id(orderId).orElse(null);
        log.info("[주문] 주문 상세 조회 완료. 사용자={}, 주문ID={} (결제 완료 화면/영수증용)", user.getEmail(), orderId);
        log.debug("getOrderDetail success: orderId={}, userId={}", orderId, user.getId());

        // 주문 상세 조회
        return orderDetailMapper.toGetOrderDetailResponse(order, storeOrderInfos, payment);
    }

    public GetStoreOrderDetailResponse getStoreOrderDetail(String email, Long storeOrderId) {

        User user = userLoader.loadUserByUsername(email);

        StoreOrder storeOrder = storeOrderRepository.findById(storeOrderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STORE_ORDER_NOT_FOUND));

        if (!storeOrder.getOrder().getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        Order order = storeOrder.getOrder();

        List<OrderProduct> orderProducts = orderProductRepository.findAllByStoreOrderOrderId(order.getId())
                .stream()
                .filter(op -> op.getStoreOrder().getId().equals(storeOrderId))
                .toList();

        Payment payment = paymentRepository.findByOrder_Id(order.getId()).orElse(null);

        return GetStoreOrderDetailResponse.builder()
                .storeOrder(storeOrderDetailMapper.toStoreOrderInfo(storeOrder))
                .order(storeOrderDetailMapper.toOrderInfo(order, user))
                .payment(storeOrderDetailMapper.toPaymentInfo(payment))
                .products(storeOrderDetailMapper.toProductInfos(orderProducts))
                .build();
    }

    public GetOrderListResponse getOrderList(
            String email,
            Pageable pageable,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String keyword) {

        User user = userLoader.loadUserByUsername(email);

        Page<StoreOrder> page =
                storeOrderRepository.findUserStoreOrders(
                        user.getId(),
                        startDate,
                        endDate,
                        keyword,
                        pageable);

        List<StoreOrder> storeOrders = page.getContent();

        if (storeOrders.isEmpty()) {
            return orderListMapper.buildResponse(page, List.of());
        }

        List<Long> storeOrderIds = storeOrders.stream()
                .map(StoreOrder::getId)
                .toList();

        List<OrderProduct> products =
                orderProductRepository.findByStoreOrderIdInWithProduct(storeOrderIds);

        Map<Long, List<OrderProduct>> productMap =
                products.stream()
                        .collect(Collectors.groupingBy(
                                p -> p.getStoreOrder().getId()));

        List<GetOrderListResponse.StoreOrderSummary> summaries = new ArrayList<>();

        for (StoreOrder storeOrder : storeOrders) {

            List<OrderProduct> orderProducts =
                    productMap.getOrDefault(storeOrder.getId(), List.of());

            List<GetOrderListResponse.ProductSummary> productSummaries =
                    orderProducts.stream()
                            .map(orderListMapper::toProductSummary)
                            .toList();

            summaries.add(orderListMapper.toStoreOrderSummary(storeOrder, productSummaries));
        }

        return orderListMapper.buildResponse(page, summaries);
    }
}
