package com.example.finalproject.payment.service;

import com.example.finalproject.delivery.service.DeliveryFeeService;
import com.example.finalproject.global.component.UserLoader;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.order.domain.Order;
import com.example.finalproject.order.domain.OrderLine;
import com.example.finalproject.order.domain.OrderProduct;
import com.example.finalproject.order.domain.StoreOrder;
import com.example.finalproject.order.enums.OrderType;
import com.example.finalproject.order.repository.OrderLineRepository;
import com.example.finalproject.order.repository.OrderProductRepository;
import com.example.finalproject.order.repository.OrderRepository;
import com.example.finalproject.order.repository.StoreOrderRepository;
import com.example.finalproject.payment.client.TossPaymentsClient;
import com.example.finalproject.payment.domain.Payment;
import com.example.finalproject.payment.dto.request.PostPaymentConfirmRequest;
import com.example.finalproject.payment.dto.request.PostPaymentPrepareRequest;
import com.example.finalproject.payment.dto.request.TossCancelRequest;
import com.example.finalproject.payment.dto.request.TossConfirmRequest;
import com.example.finalproject.payment.dto.response.PostPaymentConfirmResponse;
import com.example.finalproject.payment.dto.response.PostPaymentPrepareResponse;
import com.example.finalproject.payment.dto.response.TossCancelResponse;
import com.example.finalproject.payment.dto.response.TossConfirmResponse;
import com.example.finalproject.payment.enums.PaymentStatus;
import com.example.finalproject.payment.event.StoreOrderCreatedEvent;
import com.example.finalproject.payment.repository.PaymentRepository;
import com.example.finalproject.product.domain.Product;
import com.example.finalproject.product.repository.ProductRepository;
import com.example.finalproject.store.domain.Store;
import com.example.finalproject.store.repository.StoreRepository;
import com.example.finalproject.user.domain.Address;
import com.example.finalproject.user.domain.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final UserLoader userLoader;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderLineRepository orderLineRepository;
    private final PaymentRepository paymentRepository;
    private final DeliveryFeeService deliveryFeeService;
    private final TossPaymentsClient tossPaymentsClient;
    private final StoreOrderRepository storeOrderRepository;
    private final StoreRepository storeRepository;
    private final OrderProductRepository orderProductRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public PostPaymentPrepareResponse prepare(
            String email,
            PostPaymentPrepareRequest request) {

        User user = userLoader.loadUserByUsername(email);

        validateRequest(request);

        List<Product> products = loadAndValidateProducts(request);

        Order order = createOrder(user, request, products);

        // 주문 라인(실제 결제가 진행 전 임시 데이터 저장할 엔티티)
        createOrderLines(order, request, products);

        Payment payment = createPayment(order, request);

        return new PostPaymentPrepareResponse(
                order.getId(),
                payment.getId(),
                payment.getPgOrderId(),
                payment.getAmount()
        );
    }

    public PostPaymentConfirmResponse confirm(
            String email,
            PostPaymentConfirmRequest request) {

        User user = userLoader.loadUserByUsername(email);

        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));

        Order order = payment.getOrder();

        if (!order.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (payment.getPaymentStatus() != PaymentStatus.READY) {
            throw new BusinessException(ErrorCode.ALREADY_PROCESSED_PAYMENT);
        }

        TossConfirmResponse pg = tossPaymentsClient.confirm(
                new TossConfirmRequest(request.getPaymentKey(),
                        payment.getPgOrderId(),
                        payment.getAmount()));

        List<OrderLine> lines = orderLineRepository.findAllByOrderId(order.getId());

        try {
            for (OrderLine line : lines) {
                Product product = productRepository.findByIdForUpdate(line.getProductId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

                if (product.getStock() < line.getQuantity()) {
                    throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
                }

                product.decreaseStock(line.getQuantity());
            }
        } catch (BusinessException e) {
            TossCancelResponse tossCancelResponse = tossPaymentsClient.cancel(
                    request.getPaymentKey(),
                    new TossCancelRequest("재고 부족으로 결제 취소", payment.getAmount()));
            payment.fail("재고 부족으로 취소됨");
            throw e;
        }

        List<StoreOrder> storeOrders =
                createStoreOrdersAndOrderProducts(order, lines);

        for (StoreOrder storeOrder : storeOrders) {
            applicationEventPublisher.publishEvent(
                    new StoreOrderCreatedEvent(storeOrder.getId())
            );
        }

        payment.approve(
                request.getPaymentKey(),
                pg.getPaymentKey(),
                pg.getReceipt() != null ? pg.getReceipt().getUrl() : null
        );

        order.markPaid();

        return new PostPaymentConfirmResponse(
                order.getId(),
                payment.getId(),
                payment.getPaymentStatus().name(),
                payment.getPaidAt(),
                payment.getReceiptUrl()
        );
    }

    private List<StoreOrder> createStoreOrdersAndOrderProducts(
            Order order,
            List<OrderLine> lines) {

        Map<Long, List<OrderLine>> grouped = lines.stream()
                .collect(Collectors.groupingBy(OrderLine::getStoreId));

        List<StoreOrder> createdStoreOrders = new ArrayList<>();

        for (Map.Entry<Long, List<OrderLine>> entry : grouped.entrySet()) {

            Long storeId = entry.getKey();
            List<OrderLine> storeLines = entry.getValue();

            int storeProductPrice = storeLines.stream()
                    .mapToInt(l -> l.getPriceSnapshot() * l.getQuantity())
                    .sum();

            int deliveryFee =
                    deliveryFeeService.calculateDeliveryFee(
                            order.getUser().getId(),
                            storeId
                    );

            int finalPrice = storeProductPrice + deliveryFee;

            Store store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.STORE_NOT_FOUND));

            StoreOrder storeOrder = storeOrderRepository.save(
                    StoreOrder.builder()
                            .order(order)
                            .store(store)
                            .orderType(order.getOrderType())
                            .storeProductPrice(storeProductPrice)
                            .deliveryFee(deliveryFee)
                            .finalPrice(finalPrice)
                            .build()
            );

            createdStoreOrders.add(storeOrder);

            List<Long> productIds = storeLines.stream()
                    .map(OrderLine::getProductId)
                    .toList();

            Map<Long, Product> productMap = productRepository
                    .findAllByIdInAndDeletedAtIsNull(productIds)
                    .stream()
                    .collect(Collectors.toMap(Product::getId, Function.identity()));

            List<OrderProduct> orderProducts = storeLines.stream()
                    .map(line -> {
                        Product product = productMap.get(line.getProductId());
                        if (product == null) {
                            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
                        }
                        return OrderProduct.of(storeOrder, product, line);
                    })
                    .toList();

            orderProductRepository.saveAll(orderProducts);
        }

        return createdStoreOrders;
    }


    private void validateRequest(PostPaymentPrepareRequest request) {
        if (request.getProductQuantities() == null || request.getProductQuantities().isEmpty()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }
    }

    private List<Product> loadAndValidateProducts(PostPaymentPrepareRequest request) {

        Map<Long, Integer> quantities = request.getProductQuantities();

        List<Product> products = productRepository.findAllById(quantities.keySet());

        if (products.size() != quantities.size()) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        for (Product product : products) {

            int qty = quantities.get(product.getId());

            // 삭제 여부
            if (product.isDeleted()) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_FOUND);
            }

            // 판매 상태
            if (!Boolean.TRUE.equals(product.getIsActive())) {
                throw new BusinessException(ErrorCode.PRODUCT_NOT_AVAILABLE);
            }

            // 재고 충분
            if (product.getStock() < qty) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
            }

            // 가격 검증
            int price = product.getEffectivePrice();
            if (price <= 0) {
                throw new BusinessException(ErrorCode.INVALID_PRICE);
            }
        }

        return products;
    }

    private Order createOrder(
            User user,
            PostPaymentPrepareRequest request,
            List<Product> products) {

        Map<Long, Integer> quantities = request.getProductQuantities();

        int totalProductPrice = 0;
        for (Product product : products) {
            totalProductPrice += product.getEffectivePrice() * quantities.get(product.getId());
        }

        int deliveryFee = deliveryFeeService.calculateTotalDeliveryFee(user.getId(), products);

        int finalPrice = totalProductPrice + deliveryFee;

        if (finalPrice <= 0) {
            throw new BusinessException(ErrorCode.INVALID_ORDER_AMOUNT);
        }

        Address address = user.getAddresses().stream().filter(Address::getIsDefault).findAny()
                .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND));

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .user(user)
                .orderType(OrderType.REGULAR)
                .totalProductPrice(totalProductPrice)
                .totalDeliveryFee(deliveryFee)
                .finalPrice(finalPrice)
                .deliveryAddress(request.getDeliveryAddress())
                .deliveryRequest(request.getDeliveryRequest())
                .deliveryLocation(address.getLocation())
                .orderedAt(LocalDateTime.now())
                .build();

        return orderRepository.save(order);
    }

    private void createOrderLines(
            Order order,
            PostPaymentPrepareRequest request,
            List<Product> products) {

        Map<Long, Integer> quantities = request.getProductQuantities();

        for (Product product : products) {
            OrderLine line = OrderLine.builder()
                    .order(order)
                    .productId(product.getId())
                    .storeId(product.getStore().getId())
                    .priceSnapshot(product.getEffectivePrice())
                    .productNameSnapshot(product.getProductName())
                    .quantity(quantities.get(product.getId()))
                    .build();

            orderLineRepository.save(line);
        }
    }

    private Payment createPayment(
            Order order,
            PostPaymentPrepareRequest request) {

        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(request.getPaymentMethod())
                .amount(order.getFinalPrice())
                .paymentStatus(PaymentStatus.READY)
                .pgProvider("tosspayments")
                .pgOrderId(generatePgOrderId(order))
                .build();

        return paymentRepository.save(payment);
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }

    private String generatePgOrderId(Order order) {
        return "PG-" + order.getOrderNumber();
    }
}
