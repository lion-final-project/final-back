package com.example.finalproject.global.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * 메일 서버 설정이 없는 환경에서도 애플리케이션이 기동되도록
 * JavaMailSender 기본 빈을 제공한다.
 *
 * - spring.mail 설정이 있으면 Spring Boot 자동 설정 빈을 사용
 * - 설정이 없으면 이 fallback 빈이 등록된다.
 */
@Configuration
public class MailFallbackConfig {

    @Bean
    @ConditionalOnMissingBean(JavaMailSender.class)
    public JavaMailSender javaMailSenderFallback() {
        return new JavaMailSenderImpl();
    }
}
