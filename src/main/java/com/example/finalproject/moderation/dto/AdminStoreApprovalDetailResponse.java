package com.example.finalproject.moderation.dto;

import com.example.finalproject.moderation.enums.ApprovalStatus;
import com.example.finalproject.moderation.enums.DocumentType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminStoreApprovalDetailResponse {

    private Long approvalId;
    private ApprovalStatus status;
    private String reason;
    private LocalDateTime appliedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime heldUntil;
    private StoreInfo store;
    private List<DocumentInfo> documents;

    @Getter
    @AllArgsConstructor
    public static class StoreInfo {
        private Long storeId;
        private String storeName;
        private String businessNumber;
        private String representativeName;
        private String representativePhone;
        private String addressLine1;
        private String addressLine2;
        private String postalCode;
    }

    @Getter
    @AllArgsConstructor
    public static class DocumentInfo {
        private DocumentType documentType;
        private String documentUrl;
    }
}
