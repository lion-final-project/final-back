package com.example.finalproject.moderation.dto.admin.rider;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminRiderApprovalApproveRequest {

    // ?뱀씤 泥섎━ 愿由ъ옄 ID.
    @NotNull
    private Long adminUserId;
}

