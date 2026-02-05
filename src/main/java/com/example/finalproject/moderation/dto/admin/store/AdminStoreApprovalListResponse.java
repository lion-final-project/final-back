package com.example.finalproject.moderation.dto.admin.store;

import com.example.finalproject.moderation.enums.ApprovalStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminStoreApprovalListResponse {

    private Long approvalId;
    private Long storeId;
    private String storeName;
    private Long ownerId;
    private String ownerName;
    private ApprovalStatus status;
    private LocalDateTime appliedAt;
    private LocalDateTime heldUntil;
}

