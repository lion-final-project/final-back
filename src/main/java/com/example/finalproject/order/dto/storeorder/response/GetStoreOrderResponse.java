package com.example.finalproject.order.dto.storeorder.response;

import com.example.finalproject.delivery.enums.DeliveryStatus;
import com.example.finalproject.order.domain.OrderProduct;
import com.example.finalproject.order.domain.StoreOrder;
import com.example.finalproject.order.enums.StoreOrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 마트 대시보드용 주문 목록 응답 DTO
 */
@Getter
@Builder
public class GetStoreOrderResponse {

    private Long storeOrderId;
    private String orderNumber;
    private String orderTitle;
    private StoreOrderStatus status;
    private DeliveryStatus deliveryStatus;
    private Integer prepTime;
    private List<GetStoreOrderProductResponse> products;
    private Integer productPrice;
    private Integer finalPrice;
    private LocalDateTime orderedAt;
    private LocalDateTime acceptedAt;
    private String deliveryAddress;

    public static GetStoreOrderResponse from(StoreOrder storeOrder, List<OrderProduct> orderProducts,
            DeliveryStatus deliveryStatus) {
        List<GetStoreOrderProductResponse> productResponses = orderProducts.stream()
                .map(GetStoreOrderProductResponse::from)
                .toList();

        String orderTitle = createOrderTitle(orderProducts);

        return GetStoreOrderResponse.builder()
                .storeOrderId(storeOrder.getId())
                .orderNumber(storeOrder.getOrder().getOrderNumber())
                .orderTitle(orderTitle)
                .status(storeOrder.getStatus())
                .deliveryStatus(deliveryStatus)
                .prepTime(storeOrder.getPrepTime())
                .products(productResponses)
                .productPrice(storeOrder.getStoreProductPrice())
                .finalPrice(storeOrder.getFinalPrice())
                .orderedAt(storeOrder.getOrder().getOrderedAt())
                .acceptedAt(storeOrder.getAcceptedAt())
                .deliveryAddress(storeOrder.getOrder().getDeliveryAddress())
                .build();
    }

    private static String createOrderTitle(List<OrderProduct> orderProducts) {
        if (orderProducts.isEmpty()) {
            return "상품 없음";
        }

        String firstProductName = orderProducts.get(0).getProductNameSnapshot();
        int additionalCount = orderProducts.size() - 1;

        if (additionalCount <= 0) {
            return firstProductName;
        }
        return firstProductName + " 외 " + additionalCount + "건";
    }
}
