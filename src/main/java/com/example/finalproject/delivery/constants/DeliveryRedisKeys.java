package com.example.finalproject.delivery.constants;

public final class DeliveryRedisKeys {
    private DeliveryRedisKeys() {
    }

    /** 배달 요청 위치 GEO 키 */
    public static final String DELIVERY_GEO_KEY = "delivery:requested";

    /** 라이더 위치 GEO 키 */
    public static final String RIDER_LOC_KEY = "rider:locations";

    /** 라이더 Redis GEO member 접두사 */
    public static final String RIDER_KEY_PREFIX = "rider";

    /** 배달 Redis GEO member 접두사 */
    public static final String DELIVERY_KEY_PREFIX = "delivery";

    /** 배달 분산 락 접두사 */
    public static final String DELIVERY_LOCK_PREFIX = "lock:delivery:";

    /** 라이더 배차 현황 SET 접두사*/
    public static final String RIDER_DISPATCH_PREFIX = "RIDER:DISPATCH:";
}
