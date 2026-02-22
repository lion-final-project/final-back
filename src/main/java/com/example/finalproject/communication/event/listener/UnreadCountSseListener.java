package com.example.finalproject.communication.event.listener;

import com.example.finalproject.communication.event.UnreadCountChangedEvent;
import com.example.finalproject.global.sse.Service.SseService;
import com.example.finalproject.global.sse.enums.SseEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UnreadCountSseListener {

    private final SseService sseService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(UnreadCountChangedEvent event) {
        sseService.send(
                event.getUserId(),
                SseEventType.UNREAD_COUNT,
                event.getUnreadCount()
        );
    }
}

