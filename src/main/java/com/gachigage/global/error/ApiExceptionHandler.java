package com.gachigage.global.error;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.gachigage.global.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException ce) {
		log.warn("Error: {}", ce.getMessage());
		ErrorCode errorCode = ce.getErrorCode();

		return ResponseEntity.status(errorCode.getHttpStatus()).body(ApiResponse.fail(errorCode, ce.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleAllException(Exception ex) {
		log.error("Unexpected Server Error: ", ex);
		ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

		return ResponseEntity.status(errorCode.getHttpStatus()).body(ApiResponse.fail(errorCode));
	}
}
