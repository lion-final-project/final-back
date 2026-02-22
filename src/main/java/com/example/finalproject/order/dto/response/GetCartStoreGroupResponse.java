package com.example.finalproject.order.dto.response;


import com.example.finalproject.order.domain.CartProduct;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GetCartStoreGroupResponse {

    private Long storeId;
    private String storeName;
    private Integer deliveryFee = 3000;
    private Boolean isDeliveryAvailable = true;
    private List<GetCartItemResponse> items;
    private Integer storeProductPrice;

    public GetCartStoreGroupResponse(
            Long storeId,
            String storeName,
            Integer deliveryFee,
            Boolean isDeliveryAvailable,
            List<GetCartItemResponse> items,
            Integer storeProductPrice) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.deliveryFee = deliveryFee;
        this.isDeliveryAvailable = isDeliveryAvailable != null ? isDeliveryAvailable : false;
        this.items = items;
        this.storeProductPrice = storeProductPrice;
    }

    public static GetCartStoreGroupResponse from(List<CartProduct> group,
                                                 int deliveryFee) {
        CartProduct first = group.get(0);
        var store = first.getStore();

        Long storeId = store.getId();
        String storeName = store.getStoreName();
        Boolean isDeliveryAvailable = Boolean.TRUE.equals(store.getIsDeliveryAvailable());

        List<GetCartItemResponse> items = group.stream()
                .map(GetCartItemResponse::from)
                .toList();

        int storeTotal = items.stream()
                .mapToInt(GetCartItemResponse::getLineTotalPrice)
                .sum();

        return new GetCartStoreGroupResponse(
                storeId,
                storeName,
                deliveryFee,
                isDeliveryAvailable,
                items,
                storeTotal
        );
    }

}
