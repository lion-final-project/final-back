package com.example.finalproject.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisKeyValueAdapter.EnableKeyspaceEvents;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableRedisRepositories(enableKeyspaceEvents = EnableKeyspaceEvents.ON_STARTUP, keyspaceNotificationsConfigParameter = "")
public class RedisConfig {

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }

    /**
     * Redis 키 만료 시 RedisKeyExpiredEvent 발행 (StoreOrder TTL 자동 거절/준비완료용).
     * Redis 서버에 notify-keyspace-events Ex 설정 필요.
     * 관리형 Redis(Valkey/ElastiCache): 서버 측(파라미터 그룹)에서 'notify-keyspace-events' 값을
     * 'Ex'로 직접 설정해야 함.
     */
    @Bean
    public KeyExpirationEventMessageListener keyExpirationEventMessageListener(
            RedisMessageListenerContainer redisMessageListenerContainer) {
        return new KeyExpirationEventMessageListener(redisMessageListenerContainer);
    }
}
