package com.example.finalproject.content.dto.request;

import com.example.finalproject.content.enums.ContentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PatchBannerUpdateRequest {

    private String title;
    private String content;
    private String imageUrl;
    private String linkUrl;
    private String backgroundColor;
    private Integer displayOrder;
    private ContentStatus status;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
}
