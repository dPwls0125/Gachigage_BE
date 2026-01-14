package com.gachigage.global.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

	private final HttpStatus httpStatus;

	public CustomException(HttpStatus httpStatus) {
		super(httpStatus.getReasonPhrase());
		this.httpStatus = httpStatus;
	}
}
