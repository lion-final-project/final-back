package com.example.finalproject.global.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.example.finalproject.payment.client")
public class FeignClientConfig {
}
