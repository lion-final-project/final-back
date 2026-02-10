package com.example.finalproject.admin.store.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminStoreListItemResponse {
    private Long storeId;
    private String storeName;
    private String representativeName;
    private String ownerName;
    private String ownerPhone;
    private String addressLine1;
    private String addressLine2;
    private String status;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
