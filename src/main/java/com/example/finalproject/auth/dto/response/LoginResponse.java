package com.example.finalproject.auth.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {

    private Long userId;
    private String email;
    private String name;
    private List<String> roles;
    private String accessToken;
    private String refreshToken;
}
