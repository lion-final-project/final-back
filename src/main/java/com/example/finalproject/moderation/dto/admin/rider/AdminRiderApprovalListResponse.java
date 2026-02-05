package com.example.finalproject.moderation.dto.admin.rider;

import com.example.finalproject.moderation.enums.ApprovalStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminRiderApprovalListResponse {

    // ?뱀씤 ?대젰 ID.
    private Long approvalId;
    // ?쇱씠??ID.
    private Long riderId;
    // ?ъ슜??ID.
    private Long userId;
    // ?ъ슜???대쫫.
    private String userName;
    // ?뱀씤 ?곹깭.
    private ApprovalStatus status;
    // ?좎껌 ?쇱떆.
    private LocalDateTime appliedAt;
    // 蹂대쪟 留뚮즺 ?쇱떆.
    private LocalDateTime heldUntil;
}

