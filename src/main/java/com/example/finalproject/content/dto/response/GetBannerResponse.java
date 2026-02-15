package com.example.finalproject.content.dto.response;

import com.example.finalproject.content.domain.Banner;
import com.example.finalproject.content.enums.ContentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GetBannerResponse {

    private Long id;
    private String title;
    private String content;
    private String imageUrl;
    private String linkUrl;
    private String backgroundColor;
    private Integer displayOrder;
    private ContentStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private LocalDateTime createdAt;

    public static GetBannerResponse from(Banner banner) {
        return GetBannerResponse.builder()
                .id(banner.getId())
                .title(banner.getTitle())
                .content(banner.getContent())
                .imageUrl(banner.getImageUrl())
                .linkUrl(banner.getLinkUrl())
                .backgroundColor(banner.getBackgroundColor())
                .displayOrder(banner.getDisplayOrder())
                .status(banner.getStatus())
                .startedAt(banner.getStartedAt())
                .endedAt(banner.getEndedAt())
                .createdAt(banner.getCreatedAt())
                .build();
    }
}
