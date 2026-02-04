package com.example.finalproject.auth.dto.kakao;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

//카카오 OAuth 토큰 발급 응답 매핑 DTO.
//@see <a href="https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#request-token">카카오 토큰 응답</a>

@Data
@NoArgsConstructor
public class KakaoTokenResponseDto {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("expires_in")
    private Integer expiresIn;

    private String scope;

    @JsonProperty("refresh_token_expires_in")
    private Integer refreshTokenExpiresIn;
}
