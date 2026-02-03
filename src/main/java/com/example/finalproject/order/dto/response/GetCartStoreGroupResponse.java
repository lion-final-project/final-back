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
    private List<GetCartItemResponse> items;
    private Integer storeProductPrice;

    public GetCartStoreGroupResponse(
            Long storeId,
            String storeName,
            Integer deliveryFee,
            List<GetCartItemResponse> items,
            Integer storeProductPrice) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.deliveryFee = deliveryFee;
        this.items = items;
        this.storeProductPrice = storeProductPrice;
    }

    public static GetCartStoreGroupResponse from(List<CartProduct> group,
                                                 int deliveryFee) {
        CartProduct first = group.get(0);

        Long storeId = first.getStore().getId();
        String storeName = first.getStore().getStoreName();

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
                items,
                storeTotal
        );
    }

}
