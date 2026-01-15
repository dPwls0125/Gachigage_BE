package com.gachigage.global.error;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@WebMvcTest(controllers = ApiExceptionHandler.class)
@Import(ApiExceptionHandlerTest.TestController.class)
class ApiExceptionHandlerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	ObjectMapper objectMapper;

	// 테스트용 DTO
	static class TestDto {
		@NotBlank(message = "이름은 필수입니다")
		public String name;

		@Email(message = "이메일 형식이 아닙니다")
		public String email;
	}

	@RestController
	static class TestController {
		@GetMapping("/test/custom-exception")
		public void throwCustomException() {
			ErrorCode errorCode = ErrorCode.USER_NOT_FOUND;
			throw new CustomException(errorCode);
		}

		@PostMapping("/test/validation")
		public void testValidation(@Valid @RequestBody TestDto dto) throws Exception {
			// 일부러 빈 값을 보내면 여기 도달하지 못하고 MethodArgumentNotValidException 발생
		}

		@GetMapping("/test/unexpected-exception")
		public void throwUnexpectedException() {
			throw new RuntimeException();
		}
	}

	@Test
	@DisplayName("CustomException 발생 시 설정한 HttpStatus와 메시지가 반환")
	void handleCustomExceptionTest() throws Exception {
		ErrorCode errorCode = ErrorCode.USER_NOT_FOUND;

		mockMvc.perform(get("/test/custom-exception"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(errorCode.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(errorCode.getMessage()));
	}

	@Test
	@DisplayName("")
	void handleMethodArgumentNotValidTest() throws Exception {
		TestDto dto = new TestDto();
		mockMvc.perform(post("/test/validation").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errorCode").value("C001"));
	}

	@Test
	@DisplayName("지원하지 않는 HTTP 메서드 호출 시 handleExceptionInternal이 동작해서 405 에러를 반환")
	void handleExceptionInternalTest_MethodNotAllowed() throws Exception {
		mockMvc.perform(post("/test/unexpected-exception").contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isMethodNotAllowed())
			.andExpect(jsonPath("$.errorCode").value("C005"))
			.andExpect(jsonPath("$.message").value("지원하지 않는 HTTP 메서드입니다."));
	}

	@Test
	@DisplayName("정의되지 않은 일반 Exception 발생 시 500 에러를 반환")
	void handleAllExceptionTest() throws Exception {
		mockMvc.perform(get("/test/unexpected-exception"))
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.status").value(500))
			.andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."));
	}
}
