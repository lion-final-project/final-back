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

    // 승인 이력 ID.
    private Long approvalId;
    // 승인 상태.
    private ApprovalStatus status;
    // 승인/거절/보류 사유.
    private String reason;
    // 신청 일시.
    private LocalDateTime appliedAt;
    // 승인 처리 일시.
    private LocalDateTime approvedAt;
    // 보류 만료 일시.
    private LocalDateTime heldUntil;
    // 라이더 기본 정보.
    private RiderInfo rider;
    // 제출 서류 목록.
    private List<DocumentInfo> documents;

    @Getter
    @AllArgsConstructor
    public static class RiderInfo {
        // 라이더 ID.
        private Long riderId;
        // 사용자 ID.
        private Long userId;
        // 사용자 이름.
        private String userName;
        // 사용자 연락처.
        private String userPhone;
        // 신분증 인증 여부.
        private Boolean idCardVerified;
        // 정산 은행명.
        private String bankName;
        // 정산 계좌번호.
        private String bankAccount;
        // 예금주.
        private String accountHolder;
    }

    @Getter
    @AllArgsConstructor
    public static class DocumentInfo {
        // 서류 유형.
        private DocumentType documentType;
        // 서류 URL.
        private String documentUrl;
    }
}
