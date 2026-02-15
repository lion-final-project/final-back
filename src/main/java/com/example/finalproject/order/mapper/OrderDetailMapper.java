package com.example.finalproject.order.mapper;

import com.example.finalproject.order.domain.Order;
import com.example.finalproject.order.domain.OrderProduct;
import com.example.finalproject.order.domain.StoreOrder;
import com.example.finalproject.order.dto.response.GetOrderDetailResponse;
import com.example.finalproject.payment.domain.Payment;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OrderDetailMapper {

    public GetOrderDetailResponse toGetOrderDetailResponse(
            Order order,
            List<GetOrderDetailResponse.StoreOrderInfo> storeOrders,
            Payment payment) {
        return GetOrderDetailResponse.builder()
                .order(GetOrderDetailResponse.OrderInfo.builder()
                        .orderId(order.getId())
                        .orderNumber(order.getOrderNumber())
                        .status(order.getStatus())
                        .orderedAt(order.getOrderedAt())
                        .deliveryRequest(order.getDeliveryRequest())
                        .finalPrice(order.getFinalPrice())
                        .deliveryAddress(order.getDeliveryAddress())
                        .build())
                .storeOrders(storeOrders)
                .payment(payment == null ? null : GetOrderDetailResponse.PaymentInfo.builder()
                        .status(payment.getPaymentStatus())
                        .amount(payment.getAmount())
                        .method(payment.getPaymentMethod())
                        .receiptUrl(payment.getReceiptUrl())
                        .build())
                .build();
    }

    public GetOrderDetailResponse.StoreOrderInfo toStoreOrderInfo(
            StoreOrder storeOrder,
            List<GetOrderDetailResponse.ProductInfo> products) {
        return GetOrderDetailResponse.StoreOrderInfo.builder()
                .storeOrderId(storeOrder.getId())
                .storeId(storeOrder.getStore().getId())
                .storeName(storeOrder.getStore().getStoreName())
                .status(storeOrder.getStatus())
                .storeFinalPrice(storeOrder.getFinalPrice())
                .products(products)
                .build();
    }

    public GetOrderDetailResponse.ProductInfo toProductInfo(
            OrderProduct op) {
        return GetOrderDetailResponse.ProductInfo.builder()
                .productNameSnapshot(op.getProductNameSnapshot())
                .priceSnapshot(op.getPriceSnapshot())
                .quantity(op.getQuantity())
                .subtotal(op.getPriceSnapshot() * op.getQuantity())
                .build();
    }
}
