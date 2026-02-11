package com.example.finalproject.content.dto.response;

import com.example.finalproject.content.domain.Banner;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostBannerCreateResponse {

    private Long id;
    private String title;
    private LocalDateTime createdAt;

    public static PostBannerCreateResponse from(Banner banner) {
        return PostBannerCreateResponse.builder()
                .id(banner.getId())
                .title(banner.getTitle())
                .createdAt(banner.getCreatedAt())
                .build();
    }
}
