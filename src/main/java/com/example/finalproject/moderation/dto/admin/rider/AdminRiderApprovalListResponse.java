package com.example.finalproject.moderation.dto.admin.rider;

import com.example.finalproject.moderation.enums.ApprovalStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminRiderApprovalListResponse {

    private Long approvalId;
    private Long riderId;
    private Long userId;
    private String userName;
    private String userEmail;
    private String userPhone;
    private ApprovalStatus status;
    private LocalDateTime appliedAt;
    private LocalDateTime heldUntil;
}
