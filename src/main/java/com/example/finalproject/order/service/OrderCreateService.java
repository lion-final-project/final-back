package com.example.finalproject.order.service;

import com.example.finalproject.communication.service.OrderPaidNotificationService;
import com.example.finalproject.checkout.service.PriceCalculationResult;
import com.example.finalproject.checkout.service.PriceCalculator;
import com.example.finalproject.coupon.domain.Coupon;
import com.example.finalproject.coupon.repository.CouponRepository;
import com.example.finalproject.delivery.service.DeliveryFeeService;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.order.domain.Cart;
import com.example.finalproject.order.domain.CartProduct;
import com.example.finalproject.order.domain.Order;
import com.example.finalproject.order.domain.OrderProduct;
import com.example.finalproject.order.domain.StoreOrder;
import com.example.finalproject.order.dto.request.PostOrderRequest;
import com.example.finalproject.order.dto.response.PostOrderResponse;
import com.example.finalproject.order.enums.OrderType;
import com.example.finalproject.order.repository.CartProductRepository;
import com.example.finalproject.order.repository.CartRepository;
import com.example.finalproject.order.repository.OrderProductRepository;
import com.example.finalproject.order.repository.OrderRepository;
import com.example.finalproject.order.repository.StoreOrderRepository;
import com.example.finalproject.payment.domain.Payment;
import com.example.finalproject.payment.domain.PaymentMethod;
import com.example.finalproject.payment.repository.PaymentMethodRepository;
import com.example.finalproject.payment.repository.PaymentRepository;
import com.example.finalproject.product.domain.Product;
import com.example.finalproject.user.domain.Address;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.repository.AddressRepository;
import com.example.finalproject.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 주문 생성 서비스 (API-ORD-001). BR-O03: PriceCalculator 공용화로 최종 결제 금액 계산. 검증: 내 장바구니 소유, 수량>=1, 주소/결제수단 존재 및 본인 소유.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCreateService {

    private static final int MAX_ORDER_NUMBER_RETRY = 3;

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartProductRepository cartProductRepository;
    private final AddressRepository addressRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PriceCalculator priceCalculator;
    private final DeliveryFeeService deliveryFeeService;
    private final OrderRepository orderRepository;
    private final StoreOrderRepository storeOrderRepository;
    private final OrderProductRepository orderProductRepository;
    private final PaymentRepository paymentRepository;
    private final CouponRepository couponRepository;
    private final OrderPaidNotificationService orderPaidNotificationService;

    // 주문 생성
    @Transactional
    public PostOrderResponse createOrder(String email, PostOrderRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND));
        if (!address.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 결제 수단 조회
        PaymentMethod paymentMethod = paymentMethodRepository
                .findByIdAndUser_Id(request.getPaymentMethodId(), user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_METHOD_NOT_FOUND));

        Cart cart = cartRepository.findByUser_Email(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));

        // 장바구니 상품 조회
        List<CartProduct> cartProducts = cartProductRepository.findAllByIdIn(request.getCartItemIds());
        if (cartProducts.size() != request.getCartItemIds().size()) {
            throw new BusinessException(ErrorCode.CART_PRODUCT_NOT_FOUND);
        }
        for (CartProduct cp : cartProducts) {
            if (!cp.getCart().getId().equals(cart.getId())) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }
            if (cp.getQuantity() < 1) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
            Product p = cp.getProduct();
            if (!Boolean.TRUE.equals(p.getIsActive())) {
                throw new BusinessException(ErrorCode.PRODUCT_INACTIVE);
            }
            if (p.getStock() == null || p.getStock() < cp.getQuantity()) {
                throw new BusinessException(ErrorCode.PRODUCT_STOCK_NOT_ENOUGH);
            }
        }

        // 장바구니에 포함된 마트가 모두 배달 가능 상태인지 검증
        for (CartProduct cp : cartProducts) {
            if (!Boolean.TRUE.equals(cp.getStore().getIsDeliveryAvailable())) {
                throw new BusinessException(ErrorCode.STORE_DELIVERY_UNAVAILABLE);
            }
        }

        // 쿠폰 couponId가 있으면 해당 쿠폰 할인 적용, 없으면 0. 포인트 null 또는 0 허용
        int discount = resolveCouponDiscount(user.getId(), request.getCouponId());
        int points = request.getUsePointsOrZero();
        if (points < 0) {
            throw new BusinessException(ErrorCode.POINTS_MUST_BE_NON_NEGATIVE);
        }
        // 주문 생성 금액 계산
        List<PriceCalculator.CheckoutItem> items = cartProducts.stream()
                .map(cp -> new PriceCalculator.CheckoutItem(
                        cp.getProduct().getId(),
                        cp.getStore().getId(),
                        cp.getProduct().getEffectivePrice(),
                        cp.getQuantity()))
                .toList();
        Long addressIdForFee = address.getId();
        PriceCalculationResult priceResult = priceCalculator.calculate(
                items, storeId -> deliveryFeeService.calculateDeliveryFeeByAddress(addressIdForFee, storeId), discount,
                points);
        int productTotal = priceResult.priceSummary().productTotal();
        int deliveryTotal = priceResult.priceSummary().deliveryTotal();
        if (discount > productTotal) {
            throw new BusinessException(ErrorCode.DISCOUNT_EXCEEDS_PRODUCT_TOTAL);
        }
        if (points > productTotal + deliveryTotal - discount) {
            throw new BusinessException(ErrorCode.POINTS_EXCEED_ORDER_TOTAL);
        }
        log.info("[BR-O03] 주문 생성 금액 계산 완료. 상품총액={}, 배달비={}, 할인={}, 포인트={}, 최종결제={}",
                productTotal, deliveryTotal, priceResult.priceSummary().discount(), priceResult.priceSummary().points(),
                priceResult.priceSummary().finalTotal());

        String orderNumber = generateOrderNumber();
        String deliveryAddressStr = address.getAddressLine1()
                + (address.getAddressLine2() != null && !address.getAddressLine2().isBlank()
                ? " " + address.getAddressLine2() : "");

        // 주문 생성
        Order order = Order.builder()
                .orderNumber(orderNumber)
                .user(user)
                .orderType(OrderType.REGULAR)
                .totalProductPrice(priceResult.priceSummary().productTotal())
                .totalDeliveryFee(priceResult.priceSummary().deliveryTotal())
                .finalPrice(priceResult.priceSummary().finalTotal())
                .deliveryAddress(deliveryAddressStr)
                .deliveryLocation(address.getLocation())
                .deliveryRequest(request.getDeliveryRequestOrEmpty())
                .orderedAt(LocalDateTime.now())
                .build();
        for (int attempt = 0; attempt < MAX_ORDER_NUMBER_RETRY; attempt++) {
            try {
                order = orderRepository.save(order);
                break;
            } catch (DataIntegrityViolationException e) {
                if (attempt == MAX_ORDER_NUMBER_RETRY - 1) {
                    throw e;
                }
                // 주문 번호 중복 시 재시도
                order = Order.builder()
                        .orderNumber(generateOrderNumber())
                        .user(user)
                        .orderType(OrderType.REGULAR)
                        .totalProductPrice(priceResult.priceSummary().productTotal())
                        .totalDeliveryFee(priceResult.priceSummary().deliveryTotal())
                        .finalPrice(priceResult.priceSummary().finalTotal())
                        .deliveryAddress(deliveryAddressStr)
                        .deliveryLocation(address.getLocation())
                        .deliveryRequest(request.getDeliveryRequestOrEmpty())
                        .orderedAt(LocalDateTime.now())
                        .build();
            }
        }

        // 마트별 상품 조회
        Map<Long, List<CartProduct>> byStore = new LinkedHashMap<>();
        cartProducts.stream()
                .sorted(Comparator.comparing(cp -> cp.getStore().getId()))
                .forEach(cp -> byStore.computeIfAbsent(cp.getStore().getId(), k -> new ArrayList<>()).add(cp));

        // 마트별 상품 가격 계산
        Map<Long, PriceCalculationResult.StorePriceSummary> summaryMap = priceResult.storeSummaries().stream()
                .collect(java.util.stream.Collectors.toMap(PriceCalculationResult.StorePriceSummary::storeId, s -> s, (v1, v2) -> v1));

        // 마트별 상품 조회
        List<PostOrderResponse.StoreOrderSummary> storeOrderSummaries = new ArrayList<>();
        for (Map.Entry<Long, List<CartProduct>> entry : byStore.entrySet()) {
            List<CartProduct> group = entry.getValue();
            CartProduct first = group.get(0);
            PriceCalculationResult.StorePriceSummary storeSummary = summaryMap.get(first.getStore().getId());

            // 마트별 상품 주문 생성
            StoreOrder storeOrder = StoreOrder.builder()
                    .order(order)
                    .store(first.getStore())
                    .orderType(OrderType.REGULAR)
                    .storeProductPrice(storeSummary.storeProductPrice())
                    .deliveryFee(storeSummary.deliveryFee())
                    .finalPrice(storeSummary.storeFinalPrice())
                    .build();
            storeOrder = storeOrderRepository.save(storeOrder);

            // 마트별 상품 주문 상품 조회
            List<PostOrderResponse.ProductSummary> productSummaries = new ArrayList<>();
            for (CartProduct cp : group) {
                Product p = cp.getProduct();
                int unitPrice = p.getEffectivePrice();
                int qty = cp.getQuantity();
                p.decreaseStock(qty);

                OrderProduct op = OrderProduct.builder()
                        .storeOrder(storeOrder)
                        .product(p)
                        .productNameSnapshot(p.getProductName())
                        .priceSnapshot(unitPrice)
                        .quantity(qty)
                        .build();
                orderProductRepository.save(op);
                productSummaries.add(PostOrderResponse.ProductSummary.builder()
                        .productId(p.getId())
                        .productName(p.getProductName())
                        .unitPrice(unitPrice)
                        .quantity(qty)
                        .subtotal(unitPrice * qty)
                        .build());
            }
            // 마트별 상품 주문 상품 조회
            storeOrderSummaries.add(PostOrderResponse.StoreOrderSummary.builder()
                    .storeOrderId(storeOrder.getId())
                    .storeId(first.getStore().getId())
                    .storeName(first.getStore().getStoreName())
                    .status(storeOrder.getStatus())
                    .storeProductPrice(storeSummary.storeProductPrice())
                    .deliveryFee(storeSummary.deliveryFee())
                    .products(productSummaries)
                    .build());
        }

        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(paymentMethod.getMethodType())
                .amount(priceResult.priceSummary().finalTotal())
                .build();
        payment = paymentRepository.save(payment);

        // 결제 성공으로 주문 확정 시점에 알림 생성 트리거 
        try {
            orderPaidNotificationService.createOrderPaidNotification(
                    user.getId(), order.getId(), order.getOrderNumber(), order.getFinalPrice());
            log.info("[주문] 결제 확정 알림 생성 완료. 사용자={}, 주문ID={}", user.getEmail(), order.getId());
        } catch (Exception e) {
            log.warn("[주문] 결제 확정 알림 생성 실패(주문은 정상 생성됨). orderId={}, error={}", order.getId(), e.getMessage());
        }

        log.info("[주문] 주문 생성 완료. 사용자={}, 주문번호={}, 주문ID={}, 쿠폰할인={}원, 사용포인트={}원, 최종결제={}원",
                user.getEmail(), orderNumber, order.getId(), discount, points, order.getFinalPrice());

        return PostOrderResponse.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderType(order.getOrderType())
                .status(order.getStatus())
                .totalProductPrice(order.getTotalProductPrice())
                .totalDeliveryFee(order.getTotalDeliveryFee())
                .discountAmount(discount)
                .finalPrice(order.getFinalPrice())
                .storeOrders(storeOrderSummaries)
                .payment(PostOrderResponse.PaymentSummary.builder()
                        .paymentId(payment.getId())
                        .paymentMethod(payment.getPaymentMethod())
                        .amount(payment.getAmount())
                        .status(payment.getPaymentStatus())
                        .build())
                .orderedAt(order.getOrderedAt())
                .build();
    }

    // couponId가 있으면 해당 사용자 쿠폰의 할인 금액 반환 없으면 0
    private int resolveCouponDiscount(Long userId, Long couponId) {
        if (couponId == null) {
            return 0;
        }
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));
        if (!coupon.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.COUPON_NOT_FOUND);
        }
        return coupon.getDiscountAmount() != null ? coupon.getDiscountAmount() : 0;
    }

    // 주문 번호 생성
    private String generateOrderNumber() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        long count = orderRepository.countByOrderedAtBetween(start, end);
        String seq = String.format("%06d", count + 1);
        return "ORD-" + today.format(DateTimeFormatter.BASIC_ISO_DATE) + "-" + seq;
    }
}
