package com.example.finalproject.admin.dto.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminReportResolveRequest {

    @NotBlank(message = "처리 결과 메시지는 필수입니다.")
    @Size(max = 1000, message = "처리 결과 메시지는 1000자 이하여야 합니다.")
    private String resultMessage;
}

