package com.example.finalproject.communication.dto.response;

import com.example.finalproject.communication.domain.Notification;
import com.example.finalproject.communication.enums.NotificationRefType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String title;
    private String content;
    private NotificationRefType referenceType;
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getTitle(),
                n.getContent(),
                n.getReferenceType(),
                n.getCreatedAt()
        );
    }
}
