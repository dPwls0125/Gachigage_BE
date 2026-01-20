package com.gachigage.global.error;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gachigage.global.WithMockCustomUser;
import com.gachigage.global.config.JwtProvider;
import com.gachigage.global.config.SecurityConfig;
import com.gachigage.global.login.service.CustomOAuth2UserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@WebMvcTest(controllers = ApiExceptionHandler.class)
@Import({ApiExceptionHandlerTest.TestController.class, SecurityConfig.class})
class ApiExceptionHandlerTest {

	@MockitoBean
	private AuthenticationSuccessHandler oAuth2SuccessHandler;

	@MockitoBean
	private CustomOAuth2UserService oAuth2UserService;

	@MockitoBean
	private JwtProvider jwtProvider;

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
		public String testValidation(@Valid @RequestBody TestDto dto) throws Exception {
			// 일부러 빈 값을 보내면 여기 도달하지 못하고 MethodArgumentNotValidException 발생
			return "";
		}

		@GetMapping("/test/unexpected-exception")
		public void throwUnexpectedException() {
			throw new RuntimeException();
		}
	}

	@Test
	@DisplayName("정의한 CustomException 발생 시 설정한 HttpStatus와 메시지가 반환")
	@WithMockCustomUser(email = "my@test.com", role = "USER")
	void handleCustomExceptionTest() throws Exception {
		ErrorCode errorCode = ErrorCode.USER_NOT_FOUND;

		mockMvc.perform(get("/test/custom-exception"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(errorCode.getHttpStatus().value()))
			.andExpect(jsonPath("$.message").value(errorCode.getMessage()));
	}

	@Test
	@DisplayName("필수 값이 요청으로 들어오지 않았을 때 Exception 후 400 에러를 반환")
	@WithMockCustomUser
	void handleMethodArgumentNotValidTest() throws Exception {
		TestDto dto = new TestDto();
		mockMvc.perform(post("/test/validation").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errorCode").value("C001"));
	}

	@Test
	@DisplayName("지원하지 않는 HTTP 메서드 호출 시 handleExceptionInternal이 동작해서 405 에러를 반환")
	@WithMockCustomUser
	void handleExceptionInternalTest_MethodNotAllowed() throws Exception {
		mockMvc.perform(patch("/test/custom-exception").contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isMethodNotAllowed())
			.andExpect(jsonPath("$.errorCode").value("C005"))
			.andExpect(jsonPath("$.message").value("지원하지 않는 HTTP 메서드입니다."));
	}

	@Test
	@DisplayName("정의되지 않은 일반 Exception 발생 시 500 에러를 반환")
	@WithMockCustomUser
	void handleAllExceptionTest() throws Exception {
		mockMvc.perform(get("/test/unexpected-exception"))
			.andExpect(status().isInternalServerError())
			.andExpect(jsonPath("$.status").value(500))
			.andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."));
	}
}
