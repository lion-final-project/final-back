package com.example.finalproject.admin.rider.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminRiderListItemResponse {
    private Long riderId;
    private String name;
    private String phone;
    private String bankName;
    private String bankAccount;
    private String accountHolder;
    private String operationStatus;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
