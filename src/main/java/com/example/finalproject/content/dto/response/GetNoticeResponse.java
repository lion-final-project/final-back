package com.example.finalproject.content.dto.response;

import com.example.finalproject.content.domain.Notice;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GetNoticeResponse {
    private Long noticeId;
    private String title;
    private LocalDateTime createdAt;

    public static GetNoticeResponse from(Notice notice) {
        return GetNoticeResponse.builder()
                .noticeId(notice.getId())
                .title(notice.getTitle())
                .createdAt(notice.getCreatedAt())
                .build();
    }
}
