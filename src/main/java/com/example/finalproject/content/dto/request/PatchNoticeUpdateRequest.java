package com.example.finalproject.content.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PatchNoticeUpdateRequest {
    private String title;
    private String content;
}