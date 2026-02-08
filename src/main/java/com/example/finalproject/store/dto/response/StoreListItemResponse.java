package com.example.finalproject.store.dto.response;

import com.example.finalproject.store.domain.Store;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StoreListItemResponse {
    private Long storeId;
    private String storeName;

    public StoreListItemResponse(Long storeId, String storeName) {
        this.storeId = storeId;
        this.storeName = storeName;
    }

    public static StoreListItemResponse from(Store store) {
        return new StoreListItemResponse(store.getId(), store.getStoreName());
    }
}
