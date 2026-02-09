package com.example.finalproject.payment.enums;

public enum PaymentStatus {

    READY,              // 결제 준비 완료 (결제창 진입 전)
    PENDING,            // PG 승인 요청 중
    APPROVED,           // 결제 승인 완료

    FAILED,             // 결제 실패

    CANCELLED,          // 승인 전 취소
    PARTIAL_REFUNDED,   // 부분 환불
    REFUNDED            // 전액 환불
}
