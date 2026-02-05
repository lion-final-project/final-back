package com.example.finalproject.checkout.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetCheckoutResponse {

    private AddressInfo address;
    private PaymentInfo payment;
    private List<StoreGroup> storeGroups;
    private PriceSummary priceSummary;

    @Getter
    @Builder
    public static class AddressInfo {
        private Long addressId;
        private String addressLine1;
        private String addressLine2;
        private String recipientName;
        private String recipientPhone;
    }

    @Getter
    @Builder
    public static class PaymentInfo {
        private Long defaultPaymentMethodId;
    }

    @Getter
    @Builder
    public static class StoreGroup {
        private Long storeId;
        private String storeName;
        private Integer deliveryFee;
        private Integer storeProductPrice;
        private Integer storeFinalPrice;
        private List<Item> items;
    }

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
