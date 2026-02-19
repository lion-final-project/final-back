package com.example.finalproject.admin.dto.notification;

import com.example.finalproject.communication.enums.BroadcastRefType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminBroadcastCreateResponse {
    private Long broadcastId;
    private BroadcastRefType targetType;
    private int recipientCount;
}

