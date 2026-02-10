package com.example.finalproject.admin.store.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminStoreStatusUpdateRequest {
    @NotNull(message = "isActive는 필수입니다.")
    private Boolean isActive;

    private String reason;
}
