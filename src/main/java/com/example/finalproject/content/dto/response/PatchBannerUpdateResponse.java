package com.example.finalproject.content.dto.response;

import com.example.finalproject.content.domain.Banner;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PatchBannerUpdateResponse {

    private Long id;
    private String title;
    private LocalDateTime updatedAt;

    public static PatchBannerUpdateResponse from(Banner banner) {
        return PatchBannerUpdateResponse.builder()
                .id(banner.getId())
                .title(banner.getTitle())
                .updatedAt(banner.getUpdatedAt())
                .build();
    }
}
