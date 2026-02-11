package com.example.finalproject.checkout.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

//체크아웃 응답
@Getter
@Builder
public class GetCheckoutResponse {

    private AddressInfo address;
    private PaymentInfo payment;
    private List<StoreGroup> storeGroups;
    private PriceSummary priceSummary;
    //현재 보유 포인트(원) 결제창 표시용
    private Integer availablePoints;

    //주소 조회
    @Getter
    @Builder
    public static class AddressInfo {
        private Long addressId;
        private String addressLine1;
        private String addressLine2;
        private String recipientName;
        private String recipientPhone;
    }

    //결제 조회
    @Getter
    @Builder
    public static class PaymentInfo {
        private Long defaultPaymentMethodId;
    }

    //마트별 상품 조회
    @Getter
    @Builder
    public static class StoreGroup {
        private Long storeId;
        private String storeName;
        /** 배송지~마트 거리(km). 좌표 없으면 null. */
        private Double distanceKm;
        private Integer deliveryFee;
        private Integer storeProductPrice;
        private Integer storeFinalPrice;
        private List<Item> items;
    }

    //상품 조회
    @Getter
    @Builder
    public static class Item {
        private Long cartItemId;
        private Long productId;
        private String productName;
        private Integer unitPrice;
        private Integer quantity;
        private Integer subtotal;
        private Availability availability;
    }

    //상품 가용성
    @Getter
    @Builder
    public static class Availability {
        private boolean isAvailable;
        private AvailabilityReason reason;
    }

    public enum AvailabilityReason {
        OUT_OF_STOCK,
        INACTIVE,
        NOT_FOUND,
        EXCEEDS_STOCK
    }

    //총 가격 요약
    @Getter
    @Builder
    public static class PriceSummary {
        private Integer productTotal;
        private Integer deliveryTotal;
        private Integer discount;
        private Integer points;
        private Integer finalTotal;
    }
}
