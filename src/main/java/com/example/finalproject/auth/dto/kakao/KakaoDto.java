package com.example.finalproject.auth.dto.kakao;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class KakaoDto {

    private long id;
    private String nickname;
}
