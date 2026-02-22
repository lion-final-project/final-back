package com.example.finalproject.auth.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2RedirectUriStartupLogger {

    private final ClientRegistrationRepository clientRegistrationRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void logKakaoRedirectUri() {
        try {
            ClientRegistration kakao = clientRegistrationRepository.findByRegistrationId("kakao");
            if (kakao != null) {
                String uri = kakao.getRedirectUri();
                log.info("[카카오 OAuth2] ClientRegistration.redirectUri(기동 시 로드값) = [{}] (카카오 콘솔과 동일해야 함)", uri);
            }
        } catch (Exception e) {
            log.debug("카카오 ClientRegistration 로그 스킵: {}", e.getMessage());
        }
    }
}
