package com.example.finalproject.store.dto.response;

import com.example.finalproject.store.enums.StoreStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostStoreRegistrationResponse {

    private Long storeId;
    private Long approvalId;
    private StoreStatus status;
    private String storeName;

    public static PostStoreRegistrationResponse of(Long storeId, Long approvalId, StoreStatus status, String storeName) {
        return PostStoreRegistrationResponse.builder()
                .storeId(storeId)
                .approvalId(approvalId)
                .status(status)
                .storeName(storeName)
                .build();
    }
}
