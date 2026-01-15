package com.gachigage.global.error;

import java.util.stream.Collectors;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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
		ErrorCode errorCode = ce.getErrorCode();

		return ResponseEntity.status(errorCode.getHttpStatus()).body(ApiResponse.fail(errorCode, ce.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleAllException(Exception ex) {
		log.error("Unexpected Server Error: ", ex);
		ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;

		return ResponseEntity.status(errorCode.getHttpStatus()).body(ApiResponse.fail(errorCode));
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
		HttpHeaders headers, HttpStatusCode status, WebRequest request) {
		log.error("Unexpected Error: ", ex);

		String detailedMessage = ex.getBindingResult()
			.getAllErrors()
			.stream()
			.map(DefaultMessageSourceResolvable::getDefaultMessage)
			.collect(Collectors.joining(", "));
		Object body = ApiResponse.fail(ErrorCode.from(status), detailedMessage);

		return handleExceptionInternal(ex, body, headers, status, request);
	}

	@Override
	protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
		HttpStatusCode statusCode, WebRequest request) {
		log.error("Unexpected Error: ", ex);

		if (body == null) {
			ErrorCode errorCode = ErrorCode.from(statusCode);
			body = ApiResponse.fail(errorCode);
		}
		return super.handleExceptionInternal(ex, body, headers, statusCode, request);
	}
}
