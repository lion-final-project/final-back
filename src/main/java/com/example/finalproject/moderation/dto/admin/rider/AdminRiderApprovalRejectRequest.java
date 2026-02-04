package com.example.finalproject.moderation.dto.admin.rider;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminRiderApprovalRejectRequest {

    // 거절 처리 관리자 ID.
    @NotNull
    private Long adminUserId;

    // 거절 사유.
    @NotBlank
    private String reason;
}
