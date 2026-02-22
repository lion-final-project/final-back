package com.example.finalproject.admin.dto.notification;

import com.example.finalproject.communication.enums.BroadcastRefType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminBroadcastCreateRequest {

    @NotNull(message = "발송 대상은 필수입니다.")
    private BroadcastRefType targetType;

    @NotBlank(message = "알림 제목은 필수입니다.")
    @Size(max = 200, message = "알림 제목은 200자 이하여야 합니다.")
    private String title;

    @NotBlank(message = "알림 내용은 필수입니다.")
    @Size(max = 2000, message = "알림 내용은 2000자 이하여야 합니다.")
    private String content;
}

