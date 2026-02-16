package com.example.finalproject.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PostOrderCancelRequest {

    @NotBlank(message = "취소 사유는 필수입니다.")
    private String reason;
}
