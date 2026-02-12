package com.example.finalproject.payment.enums;

import java.util.Arrays;

public enum CardIssuer {

    IBK_BC("3K", "기업비씨"),
    GWANGJUBANK("46", "광주"),
    LOTTE("71", "롯데"),
    KDBBANK("30", "산업"),
    BC("31", "BC"),
    SAMSUNG("51", "삼성"),
    SAEMAUL("38", "새마을"),
    SHINHAN("41", "신한"),
    SHINHYEOP("62", "신협"),
    CITI("36", "씨티"),
    WOORI_BC("33", "우리"),
    WOORI("W1", "우리"),
    POST("37", "우체국"),
    SAVINGBANK("39", "저축"),
    JEONBUKBANK("35", "전북"),
    JEJUBANK("42", "제주"),
    KAKAOBANK("15", "카카오뱅크"),
    KBANK("3A", "케이뱅크"),
    TOSSBANK("24", "토스뱅크"),
    HANA("21", "하나"),
    HYUNDAI("61", "현대"),
    KOOKMIN("11", "국민"),
    NONGHYEOP("91", "농협"),
    SUHYEOP("34", "수협"),

    UNKNOWN("UNKNOWN", "알 수 없음");

    private final String code;
    private final String koreanName;

    CardIssuer(String code, String koreanName) {
        this.code = code;
        this.koreanName = koreanName;
    }

    public String getCode() {
        return code;
    }

    public String getKoreanName() {
        return koreanName;
    }

    /**
     * issuerCode → CardIssuer
     */
    public static CardIssuer fromCode(String code) {
        if (code == null) {
            return UNKNOWN;
        }

        return Arrays.stream(values())
                .filter(v -> v.code.equalsIgnoreCase(code))
                .findFirst()
                .orElse(UNKNOWN);
    }

    /**
     * issuerCode → 한글 카드사명
     */
    public static String getKoreanNameByCode(String code) {
        return fromCode(code).getKoreanName();
    }
}

