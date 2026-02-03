package com.example.finalproject.global.exception.custom;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // COMMON
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-001", "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON-002", "요청 값이 올바르지 않습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON-003", "지원하지 않는 HTTP 메서드입니다."),
    INVALID_JSON_FORMAT(HttpStatus.BAD_REQUEST, "COMMON-004", "요청 본문(JSON) 형식이 올바르지 않습니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-001", "유저를 조회할 수 없습니다"),

    // Product
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT-001", "상품을 조회할 수 없습니다"),
    PRODUCT_INACTIVE(HttpStatus.BAD_REQUEST, "PRODUCT-002", "판매 중지된 상품입니다"),
    PRODUCT_OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "PRODUCT-003", "품절된 상품입니다"),
    PRODUCT_STOCK_NOT_ENOUGH(HttpStatus.BAD_REQUEST, "PRODUCT-004", "재고가 부족합니다"),

    // Cart
    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "CART-001", "장바구니가 없습니다"),
    CART_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "CART-002", "장바구니에 해당 상품이 없습니다"),

    // Address
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "ADDRESS-001", "주소를 조회할 수 없습니다"),

    // Delivery
    DISTANCE_CALCULATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "DELIVERY-001", "배달바 계산에 실패했습니다."),
    DELIVERY_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "DELIVERY-002", "배달 가능한 지역이 아닙니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;
}

