package com.gachigage.global.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

		return ResponseEntity.status(ce.getHttpStatus())
			.body(ApiResponse.fail(ce.getHttpStatus().value(), ce.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
		MethodArgumentNotValidException me) {
		HttpStatus badRequest = HttpStatus.BAD_REQUEST;
		String message = me.getBindingResult().getAllErrors().get(0).getDefaultMessage();

		return ResponseEntity.status(badRequest).body(ApiResponse.fail(badRequest.value(), message));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleAllException(Exception ex) {
		log.error("Internal Server Error: ", ex);
		HttpStatus internalServerError = HttpStatus.INTERNAL_SERVER_ERROR;
		
		return ResponseEntity.status(internalServerError)
			.body(ApiResponse.fail(internalServerError.value(), "서버 내부에 오류가 발생했습니다."));
	}
}
