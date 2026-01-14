package com.gachigage.global.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "유저를 찾을 수 없습니다."), INTERNAL_SERVER_ERROR(
		HttpStatus.INTERNAL_SERVER_ERROR, "E999", "서버 내부에 오류가 발생했습니다.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
