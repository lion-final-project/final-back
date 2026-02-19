package com.example.finalproject.admin.dto.notification;

import com.example.finalproject.communication.enums.BroadcastRefType;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminBroadcastHistoryItemResponse {
    private Long broadcastId;
    private String title;
    private String content;
    private BroadcastRefType targetType;
    private LocalDateTime createdAt;
}

