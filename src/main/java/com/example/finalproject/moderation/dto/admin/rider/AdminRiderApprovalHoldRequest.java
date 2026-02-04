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

    // 蹂대쪟 泥섎━ 愿由ъ옄 ID.
    @NotNull
    private Long adminUserId;

    // 蹂대쪟 ?ъ쑀.
    @NotBlank
    private String reason;
}

