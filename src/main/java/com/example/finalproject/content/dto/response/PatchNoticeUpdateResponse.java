package com.example.finalproject.content.dto.response;

import com.example.finalproject.content.domain.Notice;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PatchNoticeUpdateResponse {
    private Long noticeId;
    private String title;
    private LocalDateTime updatedAt;

    public static PatchNoticeUpdateResponse from(Notice notice) {
        return PatchNoticeUpdateResponse.builder()
                .noticeId(notice.getId())
                .title(notice.getTitle())
                .updatedAt(notice.getUpdatedAt())
                .build();
    }
}