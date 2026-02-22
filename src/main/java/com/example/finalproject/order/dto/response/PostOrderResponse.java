package com.example.finalproject.order.dto.response;

import com.example.finalproject.order.enums.OrderStatus;
import com.example.finalproject.order.enums.OrderType;
import com.example.finalproject.order.enums.StoreOrderStatus;
import com.example.finalproject.payment.enums.PaymentMethodType;
import com.example.finalproject.payment.enums.PaymentStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

/**
 * 주문 생성 응답 DTO (API-ORD-001 201 Created).
 */
@Getter
@Builder
public class PostOrderResponse {

    private Long orderId;
    private String orderNumber;
    private OrderType orderType;
    private OrderStatus status;
    private Integer totalProductPrice;
    private Integer totalDeliveryFee;
    private Integer discountAmount;
    private Integer finalPrice;
    private List<StoreOrderSummary> storeOrders;
    private PaymentSummary payment;
    private LocalDateTime orderedAt;

    @Getter
    @Builder
    public static class StoreOrderSummary {
        private Long storeOrderId;
        private Long storeId;
        private String storeName;
        private StoreOrderStatus status;
        private Integer storeProductPrice;
        private Integer deliveryFee;
        private List<ProductSummary> products;
    }

    @Getter
    @Builder
    public static class ProductSummary {
        private Long productId;
        private String productName;
        private Integer unitPrice;
        private Integer quantity;
        private Integer subtotal;
    }

    @Getter
    @Builder
    public static class PaymentSummary {
        private Long paymentId;
        private PaymentMethodType paymentMethod;
        private Integer amount;
        private PaymentStatus status;
    }
}
