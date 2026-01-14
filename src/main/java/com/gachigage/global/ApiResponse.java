package com.gachigage.global;

public record ApiResponse<T>(int code, ApiStatus status, String message, T data) {
	private static final String SUCCESS_MESSAGE = "성공적으로 처리되었습니다.";

	public static <T> ApiResponse<T> success() {
		return new ApiResponse<>(200, ApiStatus.success, SUCCESS_MESSAGE, null);
	}

	public static <T> ApiResponse<T> success(T data) {
		return new ApiResponse<>(200, ApiStatus.success, SUCCESS_MESSAGE, data);
	}

	public static <T> ApiResponse<T> fail(int errorCode, String message) {
		return new ApiResponse<>(errorCode, ApiStatus.fail, message, null);
	}
}
