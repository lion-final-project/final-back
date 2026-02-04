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

        for (String emitterId : emitterIds) {
            SseEmitter emitter = repository.get(emitterId);
            if (emitter == null) {
                continue;
            }

            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(data));
            } catch (IOException e) {
                repository.remove(userId, emitterId);
            }
        }
    }
}

