package com.example.finalproject.admin.dto.user;

import com.example.finalproject.user.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminUserStatusUpdateRequest {

    @NotNull
    private UserStatus status;

    private String reason;
}
