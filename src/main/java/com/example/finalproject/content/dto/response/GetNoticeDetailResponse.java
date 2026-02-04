package com.example.finalproject.content.dto.response;

import com.example.finalproject.content.domain.Notice;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GetNoticeDetailResponse {
    private Long noticeId;
    private String title;
    private String content;
    private LocalDateTime createdAt;

    public static GetNoticeDetailResponse from(Notice notice) {
        return GetNoticeDetailResponse.builder()
                .noticeId(notice.getId())
                .title(notice.getTitle())
                .content(notice.getContent())
                .createdAt(notice.getCreatedAt())
                .build();
    }
}
