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

	// USER
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER-001", "사용자를 찾을 수 없습니다."),

	// NOTICE
	NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTICE-001", "공지사항을 찾을 수 없습니다."),
	;

	private final HttpStatus status;
	private final String code;
	private final String message;
}

