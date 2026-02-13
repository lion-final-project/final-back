package com.example.finalproject.order.mapper;

import com.example.finalproject.order.domain.OrderProduct;
import com.example.finalproject.order.domain.StoreOrder;
import com.example.finalproject.order.dto.response.GetOrderListResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class OrderListMapper {

    public GetOrderListResponse buildResponse(
            Page<StoreOrder> page,
            List<GetOrderListResponse.StoreOrderSummary> summaries) {
        return GetOrderListResponse.builder()
                .storeOrders(summaries)
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .currentPage(page.getNumber())
                .size(page.getSize())
                .build();
    }

    public GetOrderListResponse.StoreOrderSummary toStoreOrderSummary(
            StoreOrder storeOrder,
            List<GetOrderListResponse.ProductSummary> productSummaries) {

        return GetOrderListResponse.StoreOrderSummary.builder()
                .storeOrderId(storeOrder.getId())
                .storeId(storeOrder.getStore().getId())
                .storeName(storeOrder.getStore().getStoreName())
                .status(storeOrder.getStatus())
                .finalPrice(storeOrder.getFinalPrice())
                .storeImageUrl(storeOrder.getStore().getStoreImage())
                .order(
                        GetOrderListResponse.OrderInfo.builder()
                                .orderId(storeOrder.getOrder().getId())
                                .orderNumber(storeOrder.getOrder().getOrderNumber())
                                .orderedAt(storeOrder.getOrder().getOrderedAt())
                                .orderStatus(storeOrder.getOrder().getStatus())
                                .deliveryAddress(storeOrder.getOrder().getDeliveryAddress())
                                .build()
                )
                .products(productSummaries)
                .build();
    }

    public GetOrderListResponse.ProductSummary toProductSummary(OrderProduct product) {

        return GetOrderListResponse.ProductSummary.builder()
                .productNameSnapshot(product.getProductNameSnapshot())
                .priceSnapshot(product.getPriceSnapshot())
                .quantity(product.getQuantity())
                .productImageUrl(
                        product.getProduct() != null
                                ? product.getProduct().getProductImageUrl()
                                : null
                )
                .build();
    }
}
