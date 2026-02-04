package com.example.finalproject.content.dto.response;

import com.example.finalproject.content.domain.Notice;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PostNoticeCreateResponse {
    private Long noticeId;
    private String title;
    private String status;
    private LocalDateTime createdAt;

    public static PostNoticeCreateResponse from(Notice notice) {
        return PostNoticeCreateResponse.builder()
                .noticeId(notice.getId())
                .title(notice.getTitle())
                .status(notice.getStatus().name())
                .createdAt(notice.getCreatedAt())
                .build();
    }
}