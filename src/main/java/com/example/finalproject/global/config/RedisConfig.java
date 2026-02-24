package com.example.finalproject.global.config;

import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import java.time.Duration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisKeyValueAdapter.EnableKeyspaceEvents;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableRedisRepositories(enableKeyspaceEvents = EnableKeyspaceEvents.ON_STARTUP, keyspaceNotificationsConfigParameter = "")
public class RedisConfig {

  @Bean
  public LettuceConnectionFactory redisConnectionFactory(RedisProperties redisProperties) {
    RedisClusterConfiguration clusterConfig =
        new RedisClusterConfiguration(redisProperties.getCluster().getNodes());

    // ElastiCache 장애 조치(failover) 또는 노드 교체 시 자동으로 새 토폴로지를 감지합니다.
    // 이 설정이 없으면 failover 후 앱을 재시작하기 전까지 Redis 전체가 응답 불가 상태가 됩니다.
    ClusterTopologyRefreshOptions topologyRefreshOptions =
        ClusterTopologyRefreshOptions.builder()
            .enableAdaptiveRefreshTrigger(
                ClusterTopologyRefreshOptions.RefreshTrigger.MOVED_REDIRECT,
                ClusterTopologyRefreshOptions.RefreshTrigger.PERSISTENT_RECONNECTS)
            .adaptiveRefreshTriggersTimeout(Duration.ofSeconds(30))
            .enablePeriodicRefresh(Duration.ofMinutes(5))
            .build();

    ClusterClientOptions clientOptions = ClusterClientOptions.builder()
        .topologyRefreshOptions(topologyRefreshOptions)
        .build();

    LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
        .clientOptions(clientOptions)
        .useSsl()  // application-prod.yml: ssl.enabled: true
        .build();

    return new LettuceConnectionFactory(clusterConfig, clientConfig);
  }

  @Bean
  public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
    StringRedisTemplate template = new StringRedisTemplate();
    template.setConnectionFactory(connectionFactory);
    return template;
  }

  @Bean
  public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
    RedisMessageListenerContainer container = new RedisMessageListenerContainer();
    container.setConnectionFactory(connectionFactory);
    return container;
  }

  @Bean
  public KeyExpirationEventMessageListener keyExpirationEventMessageListener(
    RedisMessageListenerContainer redisMessageListenerContainer) {

    KeyExpirationEventMessageListener listener = new KeyExpirationEventMessageListener(redisMessageListenerContainer);

    // ElastiCache는 CONFIG 명령어를 차단합니다.
    // 빈 문자열로 설정해 CONFIG SET notify-keyspace-events 호출을 막습니다.
    listener.setKeyspaceNotificationsConfigParameter("");

    return listener;
  }
}
