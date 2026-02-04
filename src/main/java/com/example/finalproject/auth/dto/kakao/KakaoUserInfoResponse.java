package com.example.finalproject.auth.dto.kakao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoUserInfoResponse {

    private Long id;

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    private Properties properties;

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class KakaoAccount {
        private Profile profile;

        @Getter
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Profile {
            private String nickname;
        }
    }

    @Getter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Properties {
        private String nickname;
    }

    public String getNickname() {
        if (properties != null && properties.getNickname() != null && !properties.getNickname().isBlank()) {
            return properties.getNickname();
        }
        if (kakaoAccount != null && kakaoAccount.getProfile() != null && kakaoAccount.getProfile().getNickname() != null) {
            return kakaoAccount.getProfile().getNickname();
        }
        return "카카오사용자";
    }
}
