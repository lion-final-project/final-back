package com.example.finalproject.delivery.dto.response;

import com.example.finalproject.delivery.domain.Rider;
import com.example.finalproject.delivery.enums.RiderApprovalStatus;
import com.example.finalproject.delivery.enums.RiderOperationStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RiderResponse {

    private Long id;

    @JsonProperty(value = "user-id")
    private Long userId;
    private String name;
    private String phone;
    
    @JsonProperty(value = "bank-name")
    private String bankName;

    @JsonProperty(value = "bank-account")
    private String bankAccount;

    @JsonProperty(value = "account-holder")
    private String accountHolder;

    @JsonProperty(value = "approval-status")
    private RiderApprovalStatus status;

    @JsonProperty(value = "operation-status")
    private RiderOperationStatus operationStatus;

    public static RiderResponse from(Rider rider) {
        return RiderResponse.builder()
                .id(rider.getId())
                .userId(rider.getUser().getId())
                .name(rider.getUser().getName())
                .phone(rider.getUser().getPhone())
                .bankName(rider.getBankName())
                .bankAccount(rider.getBankAccount())
                .accountHolder(rider.getAccountHolder())
                .status(rider.getStatus())
                .operationStatus(rider.getOperationStatus())
                .build();
    }
}
