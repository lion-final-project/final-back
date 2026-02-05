package com.example.finalproject.order.service;

import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.order.domain.Order;
import com.example.finalproject.order.domain.OrderProduct;
import com.example.finalproject.order.domain.StoreOrder;
import com.example.finalproject.order.dto.response.GetOrderDetailResponse;
import com.example.finalproject.order.repository.OrderProductRepository;
import com.example.finalproject.order.repository.OrderRepository;
import com.example.finalproject.order.repository.StoreOrderRepository;
import com.example.finalproject.payment.domain.Payment;
import com.example.finalproject.payment.repository.PaymentRepository;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    public GetOrderDetailResponse getOrderDetail(String email, Long orderId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_FOUND));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        List<StoreOrder> storeOrders = storeOrderRepository.findAllByOrderId(orderId);
        List<OrderProduct> orderProducts = orderProductRepository.findAllByStoreOrderOrderId(orderId);

        Map<Long, List<OrderProduct>> productsByStoreOrder = orderProducts.stream()
                .collect(Collectors.groupingBy(op -> op.getStoreOrder().getId()));

        List<GetOrderDetailResponse.StoreOrderInfo> storeOrderInfos = storeOrders.stream()
                .map(storeOrder -> {
                    List<GetOrderDetailResponse.ProductInfo> products = productsByStoreOrder
                            .getOrDefault(storeOrder.getId(), List.of())
                            .stream()
                            .map(op -> GetOrderDetailResponse.ProductInfo.builder()
                                    .productNameSnapshot(op.getProductNameSnapshot())
                                    .priceSnapshot(op.getPriceSnapshot())
                                    .quantity(op.getQuantity())
                                    .subtotal(op.getPriceSnapshot() * op.getQuantity())
                                    .build())
                            .toList();

                    return GetOrderDetailResponse.StoreOrderInfo.builder()
                            .storeOrderId(storeOrder.getId())
                            .storeId(storeOrder.getStore().getId())
                            .storeName(storeOrder.getStore().getStoreName())
                            .status(storeOrder.getStatus())
                            .storeFinalPrice(storeOrder.getFinalPrice())
                            .products(products)
                            .build();
                })
                .toList();

        Payment payment = paymentRepository.findByOrder_Id(orderId).orElse(null);
        log.debug("getOrderDetail success: orderId={}, userId={}", orderId, user.getId());

        return GetOrderDetailResponse.builder()
                .order(GetOrderDetailResponse.OrderInfo.builder()
                        .orderId(order.getId())
                        .orderNumber(order.getOrderNumber())
                        .status(order.getStatus())
                        .orderedAt(order.getOrderedAt())
                        .deliveryRequest(order.getDeliveryRequest())
                        .finalPrice(order.getFinalPrice())
                        .deliveryAddress(order.getDeliveryAddress())
                        .build())
                .storeOrders(storeOrderInfos)
                .payment(payment == null ? null : GetOrderDetailResponse.PaymentInfo.builder()
                        .status(payment.getPaymentStatus())
                        .amount(payment.getAmount())
                        .method(payment.getPaymentMethod())
                        .receiptUrl(payment.getReceiptUrl())
                        .build())
                .build();
    }
}
