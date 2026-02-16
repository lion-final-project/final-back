package com.example.finalproject.order.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * StoreOrder 자동 상태 변경을 위한 Redis TTL 키 관리.
 * - auto_reject: PENDING 주문 5분 미응답 시 자동 거절 (만료 시각 = orderedAt + 5분, EXPIREAT)
 * - auto_ready: ACCEPTED 주문 prepTime 경과 시 자동 준비완료 (만료 시각 = acceptedAt + prepTime, EXPIREAT)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StoreOrderTtlService {

    private static final String KEY_PREFIX_AUTO_REJECT = "store_order:auto_reject:";
    private static final String KEY_PREFIX_AUTO_READY = "store_order:auto_ready:";
    private static final int AUTO_REJECT_MINUTES = 5;
    public static final String KEY_PREFIX_AUTO_REJECT_SHORT = "auto_reject:";
    public static final String KEY_PREFIX_AUTO_READY_SHORT = "auto_ready:";

    private final StringRedisTemplate redisTemplate;

    /**
     * orderedAt 기준 만료 시각(orderedAt + 5분)으로 자동 거절 키 설정.
     * 프론트 카운트다운과 시점을 맞춤.
     */
    public void setAutoReject(Long storeOrderId, LocalDateTime orderedAt) {
        if (orderedAt == null) {
            log.warn("[TTL] orderedAt이 null이라 자동 거절 키 설정 스킵 - storeOrderId={}", storeOrderId);
            return;
        }
        String key = KEY_PREFIX_AUTO_REJECT + storeOrderId;
        LocalDateTime rejectAt = orderedAt.plusMinutes(AUTO_REJECT_MINUTES);
        Date expireAt = Date.from(rejectAt.atZone(ZoneId.systemDefault()).toInstant());
        redisTemplate.opsForValue().set(key, "1");
        redisTemplate.expireAt(key, expireAt);
        log.info("[TTL][추적] 자동 거절 키 설정(EXPIREAT) - storeOrderId={}, orderedAt={}, rejectAt={}", storeOrderId, orderedAt, rejectAt);
    }

    public void removeAutoReject(Long storeOrderId) {
        String key = KEY_PREFIX_AUTO_REJECT + storeOrderId;
        redisTemplate.delete(key);
        log.debug("[TTL] 자동 거절 키 삭제 - storeOrderId={}", storeOrderId);
    }

    /**
     * acceptedAt 기준 만료 시각(acceptedAt + prepTime분)으로 자동 준비완료 키 설정.
     * 프론트 readyAt 카운트다운과 시점을 맞춤.
     */
    public void setAutoReady(Long storeOrderId, LocalDateTime acceptedAt, int prepTimeMinutes) {
        if (acceptedAt == null) {
            log.warn("[TTL] acceptedAt이 null이라 자동 준비완료 키 설정 스킵 - storeOrderId={}", storeOrderId);
            return;
        }
        String key = KEY_PREFIX_AUTO_READY + storeOrderId;
        LocalDateTime readyAt = acceptedAt.plusMinutes(prepTimeMinutes);
        Date expireAt = Date.from(readyAt.atZone(ZoneId.systemDefault()).toInstant());
        redisTemplate.opsForValue().set(key, "1");
        redisTemplate.expireAt(key, expireAt);
        log.info("[TTL][추적] 자동 준비완료 키 설정(EXPIREAT) - storeOrderId={}, acceptedAt={}, readyAt={}", storeOrderId, acceptedAt, readyAt);
    }

    public void removeAutoReady(Long storeOrderId) {
        String key = KEY_PREFIX_AUTO_READY + storeOrderId;
        redisTemplate.delete(key);
        log.debug("[TTL] 자동 준비완료 키 삭제 - storeOrderId={}", storeOrderId);
    }

    public static String getAutoRejectKeyPrefix() {
        return KEY_PREFIX_AUTO_REJECT;
    }

    public static String getAutoReadyKeyPrefix() {
        return KEY_PREFIX_AUTO_READY;
    }

    public static String getAutoRejectKeyPrefixShort() {
        return KEY_PREFIX_AUTO_REJECT_SHORT;
    }

    public static String getAutoReadyKeyPrefixShort() {
        return KEY_PREFIX_AUTO_READY_SHORT;
    }
}
