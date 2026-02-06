package com.example.finalproject.store.dto.response;

import com.example.finalproject.store.domain.Store;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetMyStoreResponse {
    private Long storeId;
    private String storeName;
    private String categoryName;

    public static GetMyStoreResponse from(Store store) {
        return GetMyStoreResponse.builder()
                .storeId(store.getId())
                .storeName(store.getStoreName())
                .categoryName(store.getStoreCategory() != null ? store.getStoreCategory().getCategoryName() : null)
                .build();
    }
}
