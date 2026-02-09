package com.example.finalproject.checkout.service;

import com.example.finalproject.checkout.dto.response.GetCheckoutResponse;
import com.example.finalproject.coupon.domain.Coupon;
import com.example.finalproject.coupon.repository.CouponRepository;
import com.example.finalproject.global.exception.custom.BusinessException;
import com.example.finalproject.global.exception.custom.ErrorCode;
import com.example.finalproject.order.domain.Cart;
import com.example.finalproject.order.domain.CartProduct;
import com.example.finalproject.order.repository.CartProductRepository;
import com.example.finalproject.order.repository.CartRepository;
import com.example.finalproject.payment.domain.PaymentMethod;
import com.example.finalproject.payment.repository.PaymentMethodRepository;
import com.example.finalproject.user.domain.Address;
import com.example.finalproject.user.domain.User;
import com.example.finalproject.user.repository.AddressRepository;
import com.example.finalproject.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CheckoutService {

    private static final int DEFAULT_DELIVERY_FEE = 3000;

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartProductRepository cartProductRepository;
    private final AddressRepository addressRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final CouponRepository couponRepository;
    private final PriceCalculator priceCalculator;

    public GetCheckoutResponse getCheckout(String email, List<Long> cartItemIds, Long addressId,
            Long couponId, Integer usePoints) {
        if (cartItemIds == null || cartItemIds.isEmpty()) {
            log.warn("getCheckout: cartItemIds empty or null");
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 장바구니 조회
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));

        // 장바구니 상품 조회
        List<CartProduct> cartProducts = cartProductRepository.findAllByIdIn(cartItemIds);
        if (cartProducts.size() != cartItemIds.size()) {
            throw new BusinessException(ErrorCode.CART_PRODUCT_NOT_FOUND);
        }

        // 장바구니 상품 소유자 확인
        for (CartProduct cartProduct : cartProducts) {
            if (!cartProduct.getCart().getId().equals(cart.getId())) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }
        }

        // 배송지 조회
        Address address = resolveAddress(user, addressId);
        // 기본 결제 수단 조회
        PaymentMethod defaultPayment = paymentMethodRepository.findFirstByUserIdAndIsDefaultTrue(user.getId()).orElse(null);

        // 장바구니 상품 그룹화
        Map<Long, List<CartProduct>> grouped = new LinkedHashMap<>();
        cartProducts.stream().sorted(Comparator.comparing(cp -> cp.getStore().getId())).forEach(cp ->
                grouped.computeIfAbsent(cp.getStore().getId(), k -> new ArrayList<>()).add(cp)
        );

        // 장바구니 상품 가격 계산
        List<PriceCalculator.CheckoutItem> calculatorItems = cartProducts.stream()
                .map(cp -> new PriceCalculator.CheckoutItem(
                        cp.getProduct().getId(),
                        cp.getStore().getId(),
                        cp.getProduct().getEffectivePrice(),
                        cp.getQuantity()
                ))
                .toList();

        // 쿠폰 
        int discount = resolveCouponDiscount(user.getId(), couponId);
        int points = usePoints != null && usePoints >= 0 ? usePoints : 0;
        int availablePoints = user.getPoints() != null ? user.getPoints() : 0;
        log.info("[결제창] 쿠폰·포인트 적용. 쿠폰ID={}, 쿠폰할인={}원, 사용포인트={}원, 보유포인트={}원",
                couponId, discount, points, availablePoints);
        // 장바구니 상품 가격 계산 (BR-O03 공용 로직)
        PriceCalculationResult result = priceCalculator.calculate(calculatorItems, storeId -> DEFAULT_DELIVERY_FEE, discount, points);
        log.info("[결제창] 금액 계산. 상품총액={}원, 배달비={}원, 할인={}원, 포인트={}원, 최종결제={}원",
                result.priceSummary().productTotal(), result.priceSummary().deliveryTotal(),
                result.priceSummary().discount(), result.priceSummary().points(), result.priceSummary().finalTotal());
        Map<Long, PriceCalculationResult.StorePriceSummary> summaryMap = result.storeSummaries().stream()
                .collect(java.util.stream.Collectors.toMap(PriceCalculationResult.StorePriceSummary::storeId, s -> s));

        // 장바구니 상품 그룹화
        List<GetCheckoutResponse.StoreGroup> storeGroups = grouped.values().stream()
                .map(group -> {
                    CartProduct first = group.getFirst();
                    PriceCalculationResult.StorePriceSummary storeSummary = summaryMap.get(first.getStore().getId());
                    List<GetCheckoutResponse.Item> items = group.stream().map(this::toItem).toList();
                    return GetCheckoutResponse.StoreGroup.builder()
                            .storeId(first.getStore().getId())
                            .storeName(first.getStore().getStoreName())
                            .deliveryFee(storeSummary.deliveryFee())
                            .storeProductPrice(storeSummary.storeProductPrice())
                            .storeFinalPrice(storeSummary.storeFinalPrice())
                            .items(items)
                            .build();
                })
                .toList();

        log.info("[결제창] 주문서 미리보기 조회 완료. 사용자={}, 상품={}건, 마트={}곳, 기본결제수단ID={}, 보유포인트={}원",
                user.getEmail(), cartProducts.size(), storeGroups.size(),
                defaultPayment != null ? defaultPayment.getId() : null, availablePoints);
        return GetCheckoutResponse.builder()
                .address(GetCheckoutResponse.AddressInfo.builder()
                        .addressId(address.getId())
                        .addressLine1(address.getAddressLine1())
                        .addressLine2(address.getAddressLine2())
                        .recipientName(address.getAddressName())
                        .recipientPhone(address.getContact())
                        .build())
                .payment(GetCheckoutResponse.PaymentInfo.builder()
                        .defaultPaymentMethodId(defaultPayment != null ? defaultPayment.getId() : null)
                        .build())
                .storeGroups(storeGroups)
                .priceSummary(GetCheckoutResponse.PriceSummary.builder()
                        .productTotal(result.priceSummary().productTotal())
                        .deliveryTotal(result.priceSummary().deliveryTotal())
                        .discount(result.priceSummary().discount())
                        .points(result.priceSummary().points())
                        .finalTotal(result.priceSummary().finalTotal())
                        .build())
                .availablePoints(availablePoints)
                .build();
    }

    //couponId가 있으면 해당 사용자 쿠폰의 할인 금액 반환, 없거나 없으면 0
    private int resolveCouponDiscount(Long userId, Long couponId) {
        if (couponId == null) {
            return 0;
        }
        return couponRepository.findById(couponId)
                .filter(c -> c.getUser().getId().equals(userId))
                .map(Coupon::getDiscountAmount)
                .orElse(0);
    }

    
    private GetCheckoutResponse.Item toItem(CartProduct cp) {
        int unitPrice = cp.getProduct().getEffectivePrice();
        GetCheckoutResponse.AvailabilityReason reason = null;
        boolean available = true;

        if (!Boolean.TRUE.equals(cp.getProduct().getIsActive())) {
            available = false;
            reason = GetCheckoutResponse.AvailabilityReason.INACTIVE;
        } else if (cp.getProduct().getStock() == null || cp.getProduct().getStock() <= 0) {
            available = false;
            reason = GetCheckoutResponse.AvailabilityReason.OUT_OF_STOCK;
        } else if (cp.getQuantity() > cp.getProduct().getStock()) {
            available = false;
            reason = GetCheckoutResponse.AvailabilityReason.EXCEEDS_STOCK;
        }

        return GetCheckoutResponse.Item.builder()
                .cartItemId(cp.getId())
                .productId(cp.getProduct().getId())
                .productName(cp.getProduct().getProductName())
                .unitPrice(unitPrice)
                .quantity(cp.getQuantity())
                .subtotal(unitPrice * cp.getQuantity())
                .availability(GetCheckoutResponse.Availability.builder()
                        .isAvailable(available)
                        .reason(reason)
                        .build())
                .build();
    }

    // 배송지 조회
    private Address resolveAddress(User user, Long addressId) {
        if (addressId != null) {
            Address selected = addressRepository.findById(addressId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND));
            if (!selected.getUser().getId().equals(user.getId())) {
                throw new BusinessException(ErrorCode.FORBIDDEN);
            }
            return selected;
        }

        // 기본 배송지 조회
        return addressRepository.findByUserOrderByIsDefaultDesc(user).stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.ADDRESS_NOT_FOUND));
    }
}
