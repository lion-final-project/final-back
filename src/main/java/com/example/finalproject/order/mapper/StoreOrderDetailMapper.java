package com.example.finalproject.order.mapper;

import com.example.finalproject.order.domain.Order;
import com.example.finalproject.order.domain.OrderProduct;
import com.example.finalproject.order.domain.StoreOrder;
import com.example.finalproject.order.dto.response.GetStoreOrderDetailResponse;
import com.example.finalproject.payment.domain.Payment;
import com.example.finalproject.user.domain.User;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class StoreOrderDetailMapper {

    public GetStoreOrderDetailResponse.StoreOrderInfo toStoreOrderInfo(
            StoreOrder storeOrder) {
        return GetStoreOrderDetailResponse.StoreOrderInfo.builder()
                .storeOrderId(storeOrder.getId())
                .storeId(storeOrder.getStore().getId())
                .storeName(storeOrder.getStore().getStoreName())
                .status(storeOrder.getStatus())
                .storeProductPrice(storeOrder.getStoreProductPrice())
                .deliveryFee(storeOrder.getDeliveryFee())
                .finalPrice(storeOrder.getFinalPrice())
                .storeImageUrl(storeOrder.getStore().getStoreImage())
                .build();
    }

    public GetStoreOrderDetailResponse.OrderInfo toOrderInfo(
            Order order,
            User user) {
        return GetStoreOrderDetailResponse.OrderInfo.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .orderedAt(order.getOrderedAt())
                .deliveryRequest(order.getDeliveryRequest())
                .deliveryAddress(order.getDeliveryAddress())
                .receiverName(user.getName())
                .receiverPhone(user.getPhone())
                .build();
    }

    public GetStoreOrderDetailResponse.PaymentInfo toPaymentInfo(
            Payment payment) {
        if (payment == null) {
            return null;
        }

        return GetStoreOrderDetailResponse.PaymentInfo.builder()
                .status(payment.getPaymentStatus())
                .amount(payment.getAmount())
                .method(payment.getPaymentMethod())
                .cardCompany(payment.getCardCompany())
                .cardNumberMasked(payment.getCardNumberMasked())
                .receiptUrl(payment.getReceiptUrl())
                .build();
    }

    public List<GetStoreOrderDetailResponse.ProductInfo> toProductInfos(
            List<OrderProduct> orderProducts
    ) {
        return orderProducts.stream()
                .map(this::toProductInfo)
                .toList();
    }

    public GetStoreOrderDetailResponse.ProductInfo toProductInfo(
            OrderProduct op) {

        return GetStoreOrderDetailResponse.ProductInfo.builder()
                .productNameSnapshot(op.getProductNameSnapshot())
                .priceSnapshot(op.getPriceSnapshot())
                .quantity(op.getQuantity())
                .subtotal(op.getPriceSnapshot() * op.getQuantity())
                .productImageUrl(
                        op.getProduct() != null
                                ? op.getProduct().getProductImageUrl()
                                : null
                )
                .build();
    }
}
