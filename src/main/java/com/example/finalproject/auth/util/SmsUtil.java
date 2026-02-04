package com.example.finalproject.auth.util;

import java.util.Random;
import org.springframework.stereotype.Component;

@Component
public class SmsUtil {

    private static final Random RANDOM = new Random();

    //인증번호 6자리 난수
    public static String generateAuthCode() {
        int code = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(code);
    }

    //인증번호 SMS 메시지 포맷
    public String makeAuthMessage(String authCode) {
        return "[동네마켓 인증번호] " + authCode + "\n본인 확인을 위해 인증번호를 입력해주세요.";
    }
}
