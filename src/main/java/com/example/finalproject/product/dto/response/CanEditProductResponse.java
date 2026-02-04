package com.example.finalproject.product.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CanEditProductResponse {

    private Boolean canEdit;
    private String reason;

    public static CanEditProductResponse of(Boolean canEdit, String reason) {
        return CanEditProductResponse.builder()
                .canEdit(canEdit)
                .reason(reason)
                .build();
    }
}
