package com.example.finalproject.global.exception.custom;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {


    // COMMON
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-000", "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON-001", "입력값이 유효하지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON-003", "지원하지 않는 HTTP 메서드입니다."),
    INVALID_JSON_FORMAT(HttpStatus.BAD_REQUEST, "COMMON-004", "요청 본문(JSON) 형식이 올바르지 않습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON-005", "리소스를 찾을 수 없습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON-006", "접근 권한이 없습니다."),

    // AUTH
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH-001", "인증이 필요합니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "AUTH-002", "이미 사용 중인 이메일입니다."),
    DUPLICATE_PHONE(HttpStatus.CONFLICT, "AUTH-003", "이미 가입된 휴대폰 번호입니다."),
    PHONE_VERIFICATION_REQUIRED(HttpStatus.UNPROCESSABLE_ENTITY, "AUTH-004", "휴대폰 인증이 필요합니다."),
    PHONE_VERIFICATION_ALREADY_USED(HttpStatus.UNPROCESSABLE_ENTITY, "AUTH-005", "이미 사용된 인증 토큰입니다."),
    PHONE_VERIFICATION_EXPIRED(HttpStatus.UNPROCESSABLE_ENTITY, "AUTH-006", "인증이 만료되었습니다. 다시 시도해주세요."),
    PHONE_VERIFICATION_RESEND_LIMIT(HttpStatus.TOO_MANY_REQUESTS, "AUTH-007", "재발송 횟수를 초과했습니다. 나중에 다시 시도해주세요."),
    PHONE_VERIFICATION_NOT_FOUND(HttpStatus.BAD_REQUEST, "AUTH-008", "인증 요청을 찾을 수 없습니다. 인증번호를 먼저 요청해주세요."),
    PHONE_VERIFICATION_MISMATCH(HttpStatus.BAD_REQUEST, "AUTH-009", "인증번호가 일치하지 않습니다."),
    REFRESH_TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "AUTH-010", "유효하지 않거나 만료된 refresh token입니다."),
    EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH-011", "해당 이메일을 찾을 수 없습니다."),
    PASSWORD_MISMATCH(HttpStatus.UNAUTHORIZED, "AUTH-012", "비밀번호가 일치하지 않습니다."),
    USER_STATUS_FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH-013", "정지되었거나 비활성화된 계정입니다."),
    REFRESH_TOKEN_MISSING(HttpStatus.BAD_REQUEST, "AUTH-014", "refresh token이 필요합니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH-015", "사용자를 찾을 수 없습니다."),

    // STORAGE
    FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "STORAGE-001", "파일 업로드에 실패했습니다."),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "STORAGE-002", "지원하지 않는 파일 형식입니다."),

    // FAQ
    FAQ_NOT_FOUND(HttpStatus.NOT_FOUND, "FAQ-001", "FAQ를 찾을 수 없습니다."),

    // NOTICE
    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTICE-001", "공지사항을 찾을 수 없습니다."),

    // Product
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT-001", "상품을 조회할 수 없습니다"),
    PRODUCT_INACTIVE(HttpStatus.BAD_REQUEST, "PRODUCT-002", "판매 중지된 상품입니다"),
    PRODUCT_OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "PRODUCT-003", "품절된 상품입니다"),
    PRODUCT_STOCK_NOT_ENOUGH(HttpStatus.BAD_REQUEST, "PRODUCT-004", "재고가 부족합니다"),
    INVALID_KEYWORD_LENGTH(HttpStatus.BAD_REQUEST, "PRODUCT-005", "검색어는 2자 이상 8자 이하여야 합니다"),

    // Cart
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "CART-001", "장바구니가 없습니다"),
    CART_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "CART-002", "장바구니에 해당 상품이 없습니다"),

    // Address
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "ADDRESS-001", "주소를 조회할 수 없습니다"),

    // Delivery
    DISTANCE_CALCULATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "DELIVERY-001", "배달바 계산에 실패했습니다."),
    DELIVERY_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "DELIVERY-002", "배달 가능한 지역이 아닙니다."),


    // Inquiry
    INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND, "INQUIRY-001", "문의를 조회할 수 없습니다."),
    INQUIRY_ALREADY_ANSWERED(HttpStatus.BAD_REQUEST, "INQUIRY-002", "이미 답변된 문의입니다."),

    // Admin
    ADMIN_AUTHORITY_REQUIRED(HttpStatus.FORBIDDEN, "ADMIN-001", "관리자만 접근 가능합니다."),

    // Notification
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "Notification-001", "알림을 조회할 수 없습니다."),
    NOTIFICATION_OWNER_MISMATCH(HttpStatus.FORBIDDEN, "Notification-002", "자신의 알림만 접근할 수 있습니다."),

    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}
