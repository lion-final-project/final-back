package com.example.finalproject.order.dto.storeorder.response;

import com.example.finalproject.order.domain.OrderProduct;
import com.example.finalproject.order.domain.StoreOrder;
import com.example.finalproject.order.enums.StoreOrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class GetCompletedStoreOrderResponse {

    private Long storeOrderId;
    private String orderNumber;
    private String orderTitle;
    private StoreOrderStatus status;
    private List<GetStoreOrderProductResponse> products;
    private Integer productPrice;
    private Integer finalPrice;
    private LocalDateTime orderedAt;
    private LocalDateTime completedAt;
    private String deliveryAddress;
    private String cancelReason;

    public static GetCompletedStoreOrderResponse from(StoreOrder storeOrder, List<OrderProduct> orderProducts) {
        List<GetStoreOrderProductResponse> productResponses = orderProducts.stream()
                .map(GetStoreOrderProductResponse::from)
                .toList();

        String orderTitle = createOrderTitle(orderProducts);

        return GetCompletedStoreOrderResponse.builder()
                .storeOrderId(storeOrder.getId())
                .orderNumber(storeOrder.getOrder().getOrderNumber())
                .orderTitle(orderTitle)
                .status(storeOrder.getStatus())
                .products(productResponses)
                .productPrice(storeOrder.getStoreProductPrice())
                .finalPrice(storeOrder.getFinalPrice())
                .orderedAt(storeOrder.getOrder().getOrderedAt())
                .completedAt(resolveCompletedAt(storeOrder))
                .deliveryAddress(storeOrder.getOrder().getDeliveryAddress())
                .cancelReason(storeOrder.getCancelReason())
                .build();
    }

    private static LocalDateTime resolveCompletedAt(StoreOrder storeOrder) {
        return switch (storeOrder.getStatus()) {
            case DELIVERED -> storeOrder.getDeliveredAt();
            case PICKED_UP -> storeOrder.getPickedUpAt();
            case CANCELLED, REJECTED -> storeOrder.getCancelledAt();
            default -> null;
        };
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
