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
	INVALID_JSON_FORMAT(HttpStatus.BAD_REQUEST, "COMMON-003", "요청 본문(JSON) 형식이 올바르지 않습니다."),
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
	
	;

	private final HttpStatus status;
	private final String code;
	private final String message;
}

