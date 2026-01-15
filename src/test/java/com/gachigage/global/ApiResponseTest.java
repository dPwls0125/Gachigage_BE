package com.gachigage.global;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.gachigage.global.error.ErrorCode;

class ApiResponseTest {
	@Test
	@DisplayName("공통 응답 객체 성공 시 생성 테스트")
	void createSuccessApiResponse() {
		// given
		String data = "Hello, World";

		// when
		ApiResponse<String> response = ApiResponse.success(data);

		// then
		assertThat(response.status()).isEqualTo(200);
		assertThat(response.data()).isEqualTo(data);
		assertThat(response.errorCode()).isEqualTo(null);
		assertThat(response.message()).isEqualTo("성공적으로 처리되었습니다.");
	}

	@Test
	@DisplayName("공통 응답 객체 데이터 없이 성공 시 생성 테스트")
	void createSuccessApiResponseWithoutData() {
		// given

		// when
		ApiResponse<Object> response = ApiResponse.success();

		// then
		assertThat(response.status()).isEqualTo(200);
		assertThat(response.data()).isEqualTo(null);
		assertThat(response.errorCode()).isEqualTo(null);
		assertThat(response.message()).isEqualTo("성공적으로 처리되었습니다.");
	}

	@Test
	@DisplayName("공통 응답 객체 실패(파라미터 1개) 시 생성 테스트")
	void createFailApiResponseWithOneParameter() {
		// given
		ErrorCode errorCode = ErrorCode.USER_NOT_FOUND;

		// when
		ApiResponse<Object> response = ApiResponse.fail(errorCode);

		// then
		assertThat(response.status()).isEqualTo(errorCode.getHttpStatus().value());
		assertThat(response.data()).isEqualTo(null);
		assertThat(response.errorCode()).isEqualTo(errorCode.getCode());
		assertThat(response.message()).isEqualTo(errorCode.getMessage());
	}

	@Test
	@DisplayName("공통 응답 객체 실패(파라미터 2개) 시 생성 테스트")
	void createFailApiResponseWithTwoParameter() {
		// given
		ErrorCode errorCode = ErrorCode.USER_NOT_FOUND;
		String message = "사용자 정의 에러 메시지";

		// when
		ApiResponse<Object> response = ApiResponse.fail(errorCode, message);

		// then
		assertThat(response.status()).isEqualTo(errorCode.getHttpStatus().value());
		assertThat(response.data()).isEqualTo(null);
		assertThat(response.errorCode()).isEqualTo(errorCode.getCode());
		assertThat(response.message()).isEqualTo(message);
		assertThat(response.message()).isNotEqualTo(errorCode.getMessage());
	}
}