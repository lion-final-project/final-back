package com.example.finalproject.delivery.dto.request;

import com.example.finalproject.delivery.enums.RiderOperationStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PatchRiderStatusRequest {
    @JsonProperty(value = "operation-status")
    private RiderOperationStatus operationStatus;
}
