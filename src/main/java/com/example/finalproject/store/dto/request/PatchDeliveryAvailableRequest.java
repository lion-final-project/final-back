package com.example.finalproject.store.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PatchDeliveryAvailableRequest {

    @NotNull(message = "배달 가능 여부는 필수입니다.")
    private Boolean deliveryAvailable;
}
