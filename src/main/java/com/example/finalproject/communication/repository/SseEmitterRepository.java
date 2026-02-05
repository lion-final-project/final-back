package com.example.finalproject.communication.repository;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class SseEmitterRepository {

    // emitterId -> 실제 SSE 연결 객체
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    // userId -> 해당 유저가 열어둔 모든 SSE emitterId
    private final Map<Long, Set<String>> userEmitters = new ConcurrentHashMap<>();

    public void save(Long userId, String emitterId, SseEmitter emitter) {
        emitters.put(emitterId, emitter);
        userEmitters
                .computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet())
                .add(emitterId);
    }

    public SseEmitter get(String emitterId) {
        return emitters.get(emitterId);
    }

    public Set<String> getEmitterIdsByUser(Long userId) {
        return userEmitters.getOrDefault(userId, Set.of());
    }

    public void remove(Long userId, String emitterId) {
        emitters.remove(emitterId);

        Set<String> ids = userEmitters.get(userId);
        if (ids != null) {
            ids.remove(emitterId);
            if (ids.isEmpty()) {
                userEmitters.remove(userId);
            }
        }
    }

    public void removeAllByUserId(Long userId) {
        Set<String> emitterIds = userEmitters.remove(userId);
        if (emitterIds == null) {
            return;
        }

        for (String emitterId : emitterIds) {
            emitters.remove(emitterId);
        }
    }
}
