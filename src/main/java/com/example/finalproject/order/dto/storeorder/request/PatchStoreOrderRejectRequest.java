package com.example.finalproject.order.dto.storeorder.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PatchStoreOrderRejectRequest {

    @NotBlank
    @Size(max = 200)
    private String reason;
}
