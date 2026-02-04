package com.example.finalproject.global.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {
    // JWT 서명용 시크릿 32자 이상
    private String secret;
    //AT 기본 30분
    private long accessTokenValiditySeconds = 1800L;
    //Refresh Token 기본 14일 
    private long refreshTokenValiditySeconds = 1209600L;
}
