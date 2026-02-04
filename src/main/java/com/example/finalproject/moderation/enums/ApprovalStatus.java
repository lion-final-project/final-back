package com.example.finalproject.moderation.enums;


import lombok.Getter;

@Getter
public enum ApprovalStatus {
    PENDING("심사중"), APPROVED("승인"), REJECTED("반려"), HELD("보류");
    private final String description;

    ApprovalStatus(String description) {
        this.description = description;
    }
}
