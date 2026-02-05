package com.example.finalproject.moderation.dto.admin.rider;

import com.example.finalproject.moderation.enums.ApprovalStatus;
import com.example.finalproject.moderation.enums.DocumentType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminRiderApprovalDetailResponse {

    // ?뱀씤 ?대젰 ID.
    private Long approvalId;
    // ?뱀씤 ?곹깭.
    private ApprovalStatus status;
    // ?뱀씤/嫄곗젅/蹂대쪟 ?ъ쑀.
    private String reason;
    // ?좎껌 ?쇱떆.
    private LocalDateTime appliedAt;
    // ?뱀씤 泥섎━ ?쇱떆.
    private LocalDateTime approvedAt;
    // 蹂대쪟 留뚮즺 ?쇱떆.
    private LocalDateTime heldUntil;
    // ?쇱씠??湲곕낯 ?뺣낫.
    private RiderInfo rider;
    // ?쒖텧 ?쒕쪟 紐⑸줉.
    private List<DocumentInfo> documents;

    @Getter
    @AllArgsConstructor
    public static class RiderInfo {
        // ?쇱씠??ID.
        private Long riderId;
        // ?ъ슜??ID.
        private Long userId;
        // ?ъ슜???대쫫.
        private String userName;
        // ?ъ슜???곕씫泥?
        private String userPhone;
        // ?좊텇利??몄쬆 ?щ?.
        private Boolean idCardVerified;
        // ?뺤궛 ??됰챸.
        private String bankName;
        // ?뺤궛 怨꾩쥖踰덊샇.
        private String bankAccount;
        // ?덇툑二?
        private String accountHolder;
    }

    @Getter
    @AllArgsConstructor
    public static class DocumentInfo {
        // ?쒕쪟 ?좏삎.
        private DocumentType documentType;
        // ?쒕쪟 URL.
        private String documentUrl;
    }
}

