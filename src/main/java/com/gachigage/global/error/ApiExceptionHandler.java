package com.gachigage.global.error;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.gachigage.global.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException ce) {
		log.warn("Error: {}", ce.getMessage());

		return ResponseEntity.status(ce.getHttpStatus())
			.body(ApiResponse.fail(ce.getHttpStatus().value(), ce.getMessage()));
	}

	// @Override
	// protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
	// 	HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
	//
	// 	// 모든 에러 메시지를 콤마로 연결
	// 	String message = ex.getBindingResult().getAllErrors().stream()
	// 		.map(error -> error.getDefaultMessage())
	// 		.collect(Collectors.joining(", "));
	//
	// 	ApiResponse<Object> fail = ApiResponse.fail(statusCode.value(), message);
	//
	// 	return handleExceptionInternal(ex, fail, headers, statusCode, request);
	// }

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleAllException(Exception ex) {
		log.error("Internal Server Error: ", ex);
		HttpStatus internalServerError = HttpStatus.INTERNAL_SERVER_ERROR;

		return ResponseEntity.status(internalServerError)
			.body(ApiResponse.fail(internalServerError.value(), "서버 내부에 오류가 발생했습니다."));
	}

	@Override
	protected ResponseEntity<Object> handleExceptionInternal(
		Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {

		if (body == null) {
			body = ApiResponse.fail(statusCode.value(), ex.getMessage() + " body = null");
		}

		return super.handleExceptionInternal(ex, body, headers, statusCode, request);
	}
}
