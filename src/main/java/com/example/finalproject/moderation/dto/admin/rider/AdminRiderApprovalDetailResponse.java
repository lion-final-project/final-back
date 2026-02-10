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

    // 승인 요청 ID
    private Long approvalId;
    // 승인 상태
    private ApprovalStatus status;
    // 보류/거절 사유
    private String reason;
    // 신청 일시
    private LocalDateTime appliedAt;
    // 승인 일시
    private LocalDateTime approvedAt;
    // 보류 만료 일시
    private LocalDateTime heldUntil;
    // 배달원 정보
    private RiderInfo rider;
    // 제출 서류 목록
    private List<DocumentInfo> documents;

    @Getter
    @AllArgsConstructor
    public static class RiderInfo {
        // 배달원 ID
        private Long riderId;
        // 사용자 ID
        private Long userId;
        // 사용자 이름
        private String userName;
        // 사용자 연락처
        private String userPhone;
        // 은행명
        private String bankName;
        // 계좌번호
        private String bankAccount;
        // 예금주
        private String accountHolder;
    }

    @Getter
    @AllArgsConstructor
    public static class DocumentInfo {
        // 서류 유형
        private DocumentType documentType;
        // 서류 URL
        private String documentUrl;
    }
}
