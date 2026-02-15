package com.example.finalproject.order.dto.response;

import com.example.finalproject.order.enums.OrderStatus;
import com.example.finalproject.order.enums.StoreOrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class GetOrderListResponse {

    private List<StoreOrderSummary> storeOrders;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int size;

    // ================= ORDER INFO =================

    @Getter
    @Builder
    public static class OrderInfo {
        private Long orderId;
        private String orderNumber;
        private LocalDateTime orderedAt;
        private OrderStatus orderStatus;
        private String deliveryAddress;
    }

    // ================= STORE ORDER =================

    @Getter
    @Builder
    public static class StoreOrderSummary {
        private Long storeOrderId;
        private Long storeId;
        private String storeName;
        private StoreOrderStatus status;
        private Integer finalPrice;
        private String storeImageUrl;
        private OrderInfo order;
        private List<ProductSummary> products;
    }

    // ================= PRODUCT =================

    @Getter
    @Builder
    public static class ProductSummary {
        private String productNameSnapshot;
        private Integer priceSnapshot;
        private Integer quantity;
        private String productImageUrl;
    }
}


