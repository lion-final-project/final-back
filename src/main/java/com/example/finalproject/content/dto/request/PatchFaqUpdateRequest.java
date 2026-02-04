package com.example.finalproject.content.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PatchFaqUpdateRequest {
    private String question;
    private String answer;
}
