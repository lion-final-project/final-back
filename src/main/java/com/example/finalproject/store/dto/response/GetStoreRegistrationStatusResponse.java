package com.example.finalproject.store.dto.response;

import com.example.finalproject.store.enums.StoreStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetStoreRegistrationStatusResponse {

    /** PENDING, APPROVED 등 */
    private String status;
    /** 입점 신청 시 작성한 상호명(마트 이름) */
    private String storeName;
    /** 입점 신청 시 작성한 대표자명 */
    private String representativeName;

    public static GetStoreRegistrationStatusResponse of(
            StoreStatus storeStatus,
            String storeName,
            String representativeName
    ) {
        return GetStoreRegistrationStatusResponse.builder()
                .status(storeStatus.name())
                .storeName(storeName)
                .representativeName(representativeName)
                .build();
    }
}
