package com.example.finalproject.store.dto.response;

import com.example.finalproject.store.enums.StoreStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreRegistrationResponse {

    private Long storeId;
    private Long approvalId;
    private StoreStatus status;

    public static StoreRegistrationResponse of(Long storeId, Long approvalId, StoreStatus status) {
        return StoreRegistrationResponse.builder()
                .storeId(storeId)
                .approvalId(approvalId)
                .status(status)
                .build();
    }
}
