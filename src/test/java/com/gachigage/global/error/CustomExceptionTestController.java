package com.gachigage.global.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomExceptionTestController {
	@GetMapping("/test/custom-exception")
	public void throwCustomException() {
		throw new CustomException(HttpStatus.BAD_REQUEST);
	}

	@GetMapping("/test/unexpected-exception")
	public void throwUnexpectedException() {
		throw new RuntimeException("예상치 못한 서버 에러");
	}
}
