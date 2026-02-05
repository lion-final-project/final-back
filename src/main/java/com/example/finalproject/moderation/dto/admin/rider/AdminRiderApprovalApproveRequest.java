package com.example.finalproject.moderation.dto.admin.rider;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminRiderApprovalApproveRequest {

    @NotNull
    private Long adminUserId;
}

