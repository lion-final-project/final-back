package com.example.finalproject.order.dto.response;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GetCartResponse {

    private Long cartId;
    private List<GetCartStoreGroupResponse> stores;
    private Integer totalProductPrice;

    public GetCartResponse(
            Long cartId,
            List<GetCartStoreGroupResponse> stores,
            Integer totalProductPrice) {
        this.cartId = cartId;
        this.stores = stores;
        this.totalProductPrice = totalProductPrice;
    }

    public static GetCartResponse empty() {
        return new GetCartResponse(null, List.of(), 0);
    }

    public static GetCartResponse of(
            Long cartId,
            List<GetCartStoreGroupResponse> stores,
            int totalProductPrice) {
        return new GetCartResponse(cartId, stores, totalProductPrice);
    }

}
