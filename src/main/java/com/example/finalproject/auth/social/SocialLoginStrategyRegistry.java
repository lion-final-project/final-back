package com.example.finalproject.auth.social;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class SocialLoginStrategyRegistry {

    private final Map<String, SocialLoginStrategy> strategyMap;

    public SocialLoginStrategyRegistry(List<SocialLoginStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        strategy -> normalize(strategy.registrationId()),
                        Function.identity()));
    }

    public SocialLoginStrategy getRequiredStrategy(String registrationId) {
        SocialLoginStrategy strategy = strategyMap.get(normalize(registrationId));
        if (strategy == null) {
            throw new IllegalArgumentException("지원하지 않는 소셜 로그인입니다: " + registrationId);
        }
        return strategy;
    }

    private String normalize(String registrationId) {
        return registrationId == null ? "" : registrationId.toLowerCase(Locale.ROOT);
    }
}
