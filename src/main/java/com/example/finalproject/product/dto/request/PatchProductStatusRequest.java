package com.example.finalproject.product.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PatchProductStatusRequest {

    @NotNull(message = "활성화 여부는 필수입니다.")
    private Boolean isActive;
}
