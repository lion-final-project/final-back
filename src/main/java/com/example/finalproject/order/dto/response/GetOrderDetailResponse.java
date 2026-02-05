package com.example.finalproject.order.dto.response;

import com.example.finalproject.order.enums.OrderStatus;
import com.example.finalproject.order.enums.StoreOrderStatus;
import com.example.finalproject.payment.enums.PaymentMethodType;
import com.example.finalproject.payment.enums.PaymentStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetOrderDetailResponse {

    private OrderInfo order;
    private List<StoreOrderInfo> storeOrders;
    private PaymentInfo payment;

    @Getter
    @Builder
    public static class OrderInfo {
        private Long orderId;
        private String orderNumber;
        private OrderStatus status;
        private LocalDateTime orderedAt;
        private String deliveryRequest;
        private Integer finalPrice;
        private String deliveryAddress;
    }

    @Getter
    @Builder
    public static class StoreOrderInfo {
        private Long storeOrderId;
        private Long storeId;
        private String storeName;
        private StoreOrderStatus status;
        private Integer storeFinalPrice;
        private List<ProductInfo> products;
    }

    @Getter
    @Builder
    public static class ProductInfo {
        private String productNameSnapshot;
        private Integer priceSnapshot;
        private Integer quantity;
        private Integer subtotal;
    }

    @Getter
    @Builder
    public static class PaymentInfo {
        private PaymentStatus status;
        private Integer amount;
        private PaymentMethodType method;
        private String receiptUrl;
    }
}
