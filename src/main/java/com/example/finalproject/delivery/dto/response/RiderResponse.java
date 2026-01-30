package com.example.finalproject.delivery.dto.response;

import com.example.finalproject.delivery.domain.Rider;
import com.example.finalproject.delivery.enums.RiderApprovalStatus;
import com.example.finalproject.delivery.enums.RiderOperationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RiderResponse {

    private Long id;
    private Long userId;
    private String name;
    private String phone;
    private String bankName;
    private String bankAccount;
    private String accountHolder;

    private RiderApprovalStatus status;
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
