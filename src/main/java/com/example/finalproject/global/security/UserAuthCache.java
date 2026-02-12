package com.example.finalproject.global.security;

import com.example.finalproject.user.enums.UserStatus;
import com.example.finalproject.user.repository.UserRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserAuthCache {

    private final UserRepository userRepository;

    @Value("${security.jwt.user-cache-ttl-seconds:30}")
    private long ttlSeconds;

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public Optional<CachedUserAuth> get(String email) {
        Instant now = Instant.now();
        CacheEntry cached = cache.get(email);
        if (cached != null && now.isBefore(cached.expireAt())) {
            return Optional.of(cached.auth());
        }

        Optional<CachedUserAuth> loaded = userRepository.findByEmail(email)
                .filter(user -> user.getStatus() == UserStatus.ACTIVE && user.getDeletedAt() == null)
                .map(user -> new CachedUserAuth(user.getEmail(), user.getTokenVersion()));

        loaded.ifPresent(value -> cache.put(email,
                new CacheEntry(value, now.plus(Duration.ofSeconds(Math.max(ttlSeconds, 1))))));

        return loaded;
    }

    public record CachedUserAuth(String email, Integer tokenVersion) {
    }

    private record CacheEntry(CachedUserAuth auth, Instant expireAt) {
    }
}
