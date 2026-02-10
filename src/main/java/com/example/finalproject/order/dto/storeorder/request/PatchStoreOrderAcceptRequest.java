package com.example.finalproject.order.dto.storeorder.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PatchStoreOrderAcceptRequest {

    @NotNull
    @Min(5)
    @Max(25)
    private Integer prepTime;
}
