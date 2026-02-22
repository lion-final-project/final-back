package com.example.finalproject.global.sse.repository;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class SseEmitterRepository {

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
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

    public Set<String> getEmitterIds(Long userId) {
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
}
