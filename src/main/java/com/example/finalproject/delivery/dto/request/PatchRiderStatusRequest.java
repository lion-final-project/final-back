package com.example.finalproject.delivery.dto.request;

import com.example.finalproject.delivery.enums.RiderOperationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PatchRiderStatusRequest {
    private RiderOperationStatus operationStatus;
}
