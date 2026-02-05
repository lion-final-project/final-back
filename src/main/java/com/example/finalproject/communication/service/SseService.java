package com.example.finalproject.communication.service;


import com.example.finalproject.communication.repository.SseEmitterRepository;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

    private static final long TIMEOUT = 60 * 60 * 1000L; // 1시간

    private final SseEmitterRepository repository;

    public SseEmitter subscribe(Long userId) {
        String emitterId = userId + "_" + UUID.randomUUID();
        SseEmitter emitter = new SseEmitter(TIMEOUT);

        repository.save(userId, emitterId, emitter);

        emitter.onCompletion(() -> repository.remove(userId, emitterId)); // 정상 종료
        emitter.onTimeout(() -> repository.remove(userId, emitterId)); // 타임 아웃
        emitter.onError(e -> repository.remove(userId, emitterId)); // 에러 발생

        try {
            emitter.send(SseEmitter.event()
                    .name("CONNECTED")
                    .data("connected"));
        } catch (IOException e) {
            repository.remove(userId, emitterId);
        }

        return emitter;
    }

    public void sendToUser(Long userId, String eventName, Object data) {
        Set<String> emitterIds = repository.getEmitterIdsByUser(userId);

        if (emitterIds.isEmpty()) {
            log.debug("[SSE] 사용자 {}에게 연결된 emitter 없음", userId);
            return;
        }

        log.debug("[SSE] 사용자 {}에게 이벤트 전송: {}, emitter 수={}",
                userId, eventName, emitterIds.size());

        for (String emitterId : emitterIds) {
            SseEmitter emitter = repository.get(emitterId);
            if (emitter == null) {
                log.warn("[SSE] emitter {}를 찾을 수 없습니다.", emitterId);
                continue;
            }

            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (IOException e) {
                log.debug("[SSE] emitter 종료됨: emitterId={}, reason={}",
                        emitterId, e.getMessage());

                try {
                    emitter.complete();
                } catch (Exception ignore) {
                }

                repository.remove(userId, emitterId);
            }
        }
    }
}

