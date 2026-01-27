package com.gachigage.global.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "유저를 찾을 수 없습니다."),
	UNAUTHORIZED_USER(HttpStatus.FORBIDDEN, "U002", "권한이 없는 유저입니다."),

	// 표준 에러
	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다."),
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C002", "인증되지 않은 사용자입니다."),
	ACCESS_DENIED(HttpStatus.FORBIDDEN, "C003", "접근 권한이 없습니다."),
	RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C004", "요청하신 리소스를 찾을 수 없습니다."),
	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C005", "지원하지 않는 HTTP 메서드입니다."),
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C006", "서버 내부 오류가 발생했습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

	public static ErrorCode from(HttpStatusCode status) {
		if (status instanceof HttpStatus httpStatus) {
			return switch (httpStatus) {
				case BAD_REQUEST -> INVALID_INPUT_VALUE;
				case UNAUTHORIZED -> UNAUTHORIZED;
				case FORBIDDEN -> ACCESS_DENIED;
				case NOT_FOUND -> RESOURCE_NOT_FOUND;
				case METHOD_NOT_ALLOWED -> METHOD_NOT_ALLOWED;
				default -> INTERNAL_SERVER_ERROR;
			};
		}
		return INTERNAL_SERVER_ERROR;
	}
}
