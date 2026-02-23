package com.example.finalproject.delivery.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@AllArgsConstructor
@ToString
public class RiderApprovalResponse {

    private Long approvalId;
    private Long userId;
    private String name;
    private String phone;

    private String bankName;
    private String bankAccount;
    private String accountHolder;

    @Builder.Default
    List<String> documents = new ArrayList<>();

    private String status;
    private String reason;
    private LocalDateTime heldUntil;
}
