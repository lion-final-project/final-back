package com.example.finalproject.store.dto.response;

import com.example.finalproject.store.enums.StoreStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetStoreRegistrationStatusResponse {

    private String status;
    private String storeName;
    private String representativeName;
    private Long approvalId;
    private String reason;
    private LocalDateTime heldUntil;

    public static GetStoreRegistrationStatusResponse of(
            StoreStatus storeStatus,
            String storeName,
            String representativeName,
            Long approvalId,
            String reason,
            LocalDateTime heldUntil
    ) {
        return GetStoreRegistrationStatusResponse.builder()
                .status(storeStatus.name())
                .storeName(storeName)
                .representativeName(representativeName)
                .approvalId(approvalId)
                .reason(reason)
                .heldUntil(heldUntil)
                .build();
    }

    public static GetStoreRegistrationStatusResponse of(
            String status,
            String storeName,
            String representativeName,
            Long approvalId,
            String reason,
            LocalDateTime heldUntil
    ) {
        return GetStoreRegistrationStatusResponse.builder()
                .status(status)
                .storeName(storeName)
                .representativeName(representativeName)
                .approvalId(approvalId)
                .reason(reason)
                .heldUntil(heldUntil)
                .build();
    }

    public static GetStoreRegistrationStatusResponse none() {
        return GetStoreRegistrationStatusResponse.builder()
                .status("NONE")
                .storeName(null)
                .representativeName(null)
                .approvalId(null)
                .reason(null)
                .heldUntil(null)
                .build();
    }
}
