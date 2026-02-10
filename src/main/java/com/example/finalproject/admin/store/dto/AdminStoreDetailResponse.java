package com.example.finalproject.admin.store.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminStoreDetailResponse {
    private Long storeId;
    private String storeName;
    private String phone;
    private String description;
    private String representativeName;
    private String representativePhone;
    private String postalCode;
    private String addressLine1;
    private String addressLine2;
    private String storeImage;

    private String ownerName;
    private String ownerEmail;
    private String ownerPhone;

    private String businessOwnerName;
    private String businessNumber;
    private String telecomSalesReportNumber;

    private String settlementBankName;
    private String settlementBankAccount;
    private String settlementAccountHolder;

    private String status;
    private Boolean isActive;
    private String statusReason;
    private BigDecimal commissionRate;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
