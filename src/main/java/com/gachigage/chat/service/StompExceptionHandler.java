package com.gachigage.chat.service;

import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

import com.gachigage.global.ApiResponse;
import com.gachigage.global.error.CustomException;
import com.gachigage.global.error.ErrorCode;

@ControllerAdvice
public class StompExceptionHandler {

	@MessageExceptionHandler(CustomException.class)
	@SendToUser("/queue/errors")
	public ApiResponse<String> handleStompCustomException(CustomException ce) {
		return ApiResponse.fail(ce.getErrorCode(), ce.getMessage());
	}

	@MessageExceptionHandler(Exception.class)
	@SendToUser("/queue/errors")
	public ApiResponse<String> handleStompGeneralException(Exception ex) {
		return ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR, "알 수 없는 오류가 STOMP에 발생했습니다.");
	}
}
