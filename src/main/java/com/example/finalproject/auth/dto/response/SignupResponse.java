package com.example.finalproject.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

//회원가입 응답!
@Getter
@AllArgsConstructor
public class SignupResponse {
    private Long userId;
    private String email;
    private String name;
    private List<String> roles;

    @JsonIgnore
    private String accessToken;

    @JsonIgnore
    private String refreshToken;
}
