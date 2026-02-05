package com.example.finalproject.moderation.dto.admin.rider;

import com.example.finalproject.moderation.enums.ApprovalStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminRiderApprovalListResponse {

    // 승인 요청 ID
    private Long approvalId;
    // 배달원 ID
    private Long riderId;
    // 사용자 ID
    private Long userId;
    // 사용자 이름
    private String userName;
    // 승인 상태
    private ApprovalStatus status;
    // 신청 일시
    private LocalDateTime appliedAt;
    // 보류 만료 일시
    private LocalDateTime heldUntil;
}
