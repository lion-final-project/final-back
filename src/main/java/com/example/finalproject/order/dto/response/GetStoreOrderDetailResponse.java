package com.example.finalproject.order.dto.response;

import com.example.finalproject.order.enums.StoreOrderStatus;
import com.example.finalproject.payment.enums.PaymentMethodType;
import com.example.finalproject.payment.enums.PaymentStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetStoreOrderDetailResponse {

    private StoreOrderInfo storeOrder;
    private OrderInfo order;
    private PaymentInfo payment;
    private List<ProductInfo> products;

    @Getter
    @Builder
    public static class StoreOrderInfo {
        private Long storeOrderId;
        private Long storeId;
        private String storeName;
        private StoreOrderStatus status;
        private Integer storeProductPrice;
        private Integer deliveryFee;
        private Integer finalPrice;
        private String storeImageUrl;
    }

    @Getter
    @Builder
    public static class OrderInfo {
        private Long orderId;
        private String orderNumber;
        private LocalDateTime orderedAt;
        private String deliveryRequest;
        private String deliveryAddress;
        private String receiverName;
        private String receiverPhone;
    }

    @Getter
    @Builder
    public static class PaymentInfo {
        private PaymentStatus status;
        private Integer amount;
        private PaymentMethodType method;
        private String cardCompany;
        private String cardNumberMasked;
        private String receiptUrl;
    }

    @Getter
    @Builder
    public static class ProductInfo {
        private Long productId;
        private String productNameSnapshot;
        private Integer priceSnapshot;
        private Integer quantity;
        private Integer subtotal;
        private String productImageUrl;
    }
}
