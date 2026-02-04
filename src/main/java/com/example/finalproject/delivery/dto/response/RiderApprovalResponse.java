package com.example.finalproject.delivery.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@ToString
public class RiderApprovalResponse {

    private Long approvalId;
    private Long userId;
    private String name;
    private String phone;

    // 정산 계좌 정보
    private String bankName;
    private String bankAccount;
    // 예금주명
    private String accountHolder;

    @Builder.Default
    List<String> documents = new ArrayList<>();

    // 신청 현황
    private String status;
}
