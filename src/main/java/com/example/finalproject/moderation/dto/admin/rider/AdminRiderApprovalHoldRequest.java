package com.example.finalproject.moderation.dto.admin.rider;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminRiderApprovalHoldRequest {

    // 보류 처리 관리자 ID.
    @NotNull
    private Long adminUserId;

    // 보류 사유.
    @NotBlank
    private String reason;
}
