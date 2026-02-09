package com.gachigage.global;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gachigage.global.error.ErrorCode;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공통 응답 객체 (성공/실패)")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
	@Schema(description = "에러 코드 (실패 시에만 존재)", example = "U001", nullable = true) String errorCode,
	@Schema(description = "HTTP 상태 코드", example = "200") int status,
	@Schema(description = "응답 메시지", example = SUCCESS_MESSAGE) String message,
	@Schema(description = "응답 데이터 (성공 시에만 존재)", nullable = true) T data) {
	private static final String SUCCESS_MESSAGE = "성공적으로 처리되었습니다.";

	public static <T> ApiResponse<T> success() {
		return new ApiResponse<>(null, 200, SUCCESS_MESSAGE, null);
	}

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(null, 200, SUCCESS_MESSAGE, data);
	}

	public static <T> ApiResponse<T> fail(ErrorCode errorCode) {
		return new ApiResponse<>(errorCode.getCode(), errorCode.getHttpStatus().value(), errorCode.getMessage(), null);
	}

	public static <T> ApiResponse<T> fail(ErrorCode errorCode, String message) {
		return new ApiResponse<>(errorCode.getCode(), errorCode.getHttpStatus().value(), message, null);
	}
}
