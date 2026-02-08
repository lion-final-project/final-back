package com.example.finalproject.communication.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UnreadCountChangedEvent {
    private final Long userId;
    private final Integer unreadCount;
}
