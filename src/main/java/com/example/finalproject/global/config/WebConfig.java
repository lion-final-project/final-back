package com.example.finalproject.global.config;

import com.example.finalproject.delivery.enums.RiderOperationStatus;
import com.example.finalproject.moderation.enums.ApplicantType;
import com.example.finalproject.moderation.enums.DocumentType;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // 개별 Enum 컨버터 등록 store -> ApplicantType.STORE
        registry.addConverter(String.class, ApplicantType.class, ApplicantType::from);
        registry.addConverter(String.class, DocumentType.class, DocumentType::from);
    }
}
