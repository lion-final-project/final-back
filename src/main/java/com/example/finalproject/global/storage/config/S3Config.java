package com.example.finalproject.global.storage.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {
    // Spring Cloud AWS 3 버전 이후 에서는 application.yml의 프로퍼티를 통해 S3Client가 자동 생성
    // 특수한 설정이 없으면 따로 S3Client 빈 등록 생략 가능
}
