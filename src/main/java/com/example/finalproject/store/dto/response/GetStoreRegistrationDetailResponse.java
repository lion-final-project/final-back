package com.example.finalproject.store.dto.response;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetStoreRegistrationDetailResponse {

    private String status;
    private Long approvalId;
    private String reason;
    private LocalDateTime heldUntil;

    private String storeCategory;
    private String storeOwnerName;
    private String storeName;
    private String representativeName;
    private String representativePhone;
    private String storePhone;
    private String storeDescription;
    private String storeImageUrl;

    private String businessNumber;
    private String telecomSalesReportNumber;

    private String postalCode;
    private String addressLine1;
    private String addressLine2;
    private Double latitude;
    private Double longitude;

    private String settlementBankName;
    private String settlementBankAccount;
    private String settlementAccountHolder;

    private Map<String, String> documents;
}
