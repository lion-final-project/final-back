package com.example.finalproject.order.listener;

import com.example.finalproject.order.service.StoreOrderService;
import com.example.finalproject.order.service.StoreOrderTtlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StoreOrderTtlExpirationListener {

    private final StoreOrderService storeOrderService;

    @EventListener
    public void onRedisKeyExpired(RedisKeyExpiredEvent<?> event) {
        String key = getKeyFromEvent(event);
        if (key == null || key.isEmpty()) {
            log.info("[TTL][추적] 키 만료 이벤트 수신 - key 비어있음, 무시");
            return;
        }
        log.info("[TTL][추적] Redis 키 만료 이벤트 수신 - key={}", key);
        String rejectPrefixFull = StoreOrderTtlService.getAutoRejectKeyPrefix();
        String readyPrefixFull = StoreOrderTtlService.getAutoReadyKeyPrefix();
        String rejectPrefixShort = StoreOrderTtlService.getAutoRejectKeyPrefixShort();
        String readyPrefixShort = StoreOrderTtlService.getAutoReadyKeyPrefixShort();

        Long rejectId = parseStoreOrderIdFromKey(key, rejectPrefixFull, rejectPrefixShort);
        if (rejectId != null) {
            try {
                log.info("[TTL][추적] 자동 거절 처리 시작 - storeOrderId={}", rejectId);
                storeOrderService.processAutoRejectByTtl(rejectId);
                log.info("[TTL][추적] 자동 거절 처리 완료(정상) - storeOrderId={}", rejectId);
            } catch (NumberFormatException e) {
                log.warn("[TTL] 자동 거절 키 파싱 실패 - key={}", key, e);
            } catch (Exception e) {
                log.error("[TTL][추적] 자동 거절 처리 중 예외 - storeOrderId 파싱됐으나 processAutoRejectByTtl 실패, key={}", key, e);
                throw e;
            }
            return;
        }

        Long readyId = parseStoreOrderIdFromKey(key, readyPrefixFull, readyPrefixShort);
        if (readyId != null) {
            try {
                log.info("[TTL][추적] 자동 준비완료 처리 시작 - storeOrderId={}", readyId);
                storeOrderService.processAutoMarkReadyByTtl(readyId);
                log.info("[TTL][추적] 자동 준비완료 처리 완료(정상) - storeOrderId={}", readyId);
            } catch (NumberFormatException e) {
                log.warn("[TTL] 자동 준비완료 키 파싱 실패 - key={}", key, e);
            } catch (Exception e) {
                log.error("[TTL][추적] 자동 준비완료 처리 중 예외 - key={}", key, e);
                throw e;
            }
        }
    }

    /**
     * 드라이버/버전에 따라 key가 "store_order:auto_reject:12" 또는 "auto_reject:12" 형태로 올 수 있음.
     * 두 경우 모두 처리.
     */
    private static Long parseStoreOrderIdFromKey(String key, String prefixFull, String prefixShort) {
        String suffix = null;
        if (key.startsWith(prefixFull)) {
            suffix = key.substring(prefixFull.length());
        } else if (key.startsWith(prefixShort)) {
            suffix = key.substring(prefixShort.length());
        }
        if (suffix == null || suffix.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(suffix.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String getKeyFromEvent(RedisKeyExpiredEvent<?> event) {
        if (event.getId() != null) {
            return new String(event.getId());
        }
        Object source = event.getSource();
        if (source instanceof byte[]) {
            return new String((byte[]) source);
        }
        return null;
    }
}
