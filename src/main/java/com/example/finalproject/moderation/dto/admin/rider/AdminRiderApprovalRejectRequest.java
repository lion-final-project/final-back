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

    // 嫄곗젅 泥섎━ 愿由ъ옄 ID.
    @NotNull
    private Long adminUserId;

    // 嫄곗젅 ?ъ쑀.
    @NotBlank
    private String reason;
}

