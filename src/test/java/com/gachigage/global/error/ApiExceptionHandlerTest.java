package com.gachigage.global.error;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ApiExceptionHandler.class)
@Import(CustomExceptionTestController.class)
class ApiExceptionHandlerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	@DisplayName("CustomException 발생 시 설정한 HttpStatus와 메시지가 반환")
	void handleCustomExceptionTest() throws Exception {
		HttpStatus badRequest = HttpStatus.BAD_REQUEST;

		mockMvc.perform(get("/test/custom-exception"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value("fail"))
			.andExpect(jsonPath("$.message").value(badRequest.getReasonPhrase()));
	}

	@Test
	@DisplayName("정의되지 않은 일반 Exception 발생 시 500 에러를 반환")
	void handleAllExceptionTest() throws Exception {
		mockMvc.perform(get("/test/unexpected-exception"))
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.status").value("fail"))
			.andExpect(jsonPath("$.message").value("서버 내부에 오류가 발생했습니다."));
	}
}