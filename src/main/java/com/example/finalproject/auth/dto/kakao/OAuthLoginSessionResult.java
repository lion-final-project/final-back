package com.example.finalproject.auth.dto.kakao;

import com.example.finalproject.user.domain.User;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class OAuthLoginSessionResult {

    private final User user;
    private final List<String> roles;
}
