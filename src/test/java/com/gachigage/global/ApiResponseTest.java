package com.gachigage.global;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class ApiResponseTest {
	@Test
	@DisplayName("공통 응답 객체 성공 시 생성 테스트")
	void createSuccessApiResponse() {
		// given
		String data = "Hello, World";

		// when
		ApiResponse<String> response = ApiResponse.success(data);

		// then
		assertThat(response.status()).isEqualTo(ApiStatus.success);
		assertThat(response.data()).isEqualTo(data);
		assertThat(response.code()).isEqualTo(200);
		assertThat(response.message()).isEqualTo("성공적으로 처리되었습니다.");
	}

	@Test
	@DisplayName("공통 응답 객체 데이터 없이 성공 시 생성 테스트")
	void createSuccessApiResponseWithoutData() {
		// given

		// when
		ApiResponse<Object> response = ApiResponse.success();

		// then
		assertThat(response.status()).isEqualTo(ApiStatus.success);
		assertThat(response.data()).isEqualTo(null);
		assertThat(response.code()).isEqualTo(200);
		assertThat(response.message()).isEqualTo("성공적으로 처리되었습니다.");
	}

	@Test
	@DisplayName("공통 응답 객체 실패 시 생성 테스트")
	void createFailApiResponse() {
		// given
		HttpStatus status = HttpStatus.BAD_REQUEST;
		int errorCode = status.value();
		String message = status.getReasonPhrase();

		// when
		ApiResponse<Object> response = ApiResponse.fail(errorCode, message);

		// then
		assertThat(response.status()).isEqualTo(ApiStatus.fail);
		assertThat(response.data()).isEqualTo(null);
		assertThat(response.code()).isEqualTo(errorCode);
		assertThat(response.message()).isEqualTo(message);
	}
}