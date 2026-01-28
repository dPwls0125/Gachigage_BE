package com.gachigage.global.login.service;

import static org.mockito.BDDMockito.*;

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import com.gachigage.global.config.JwtProvider;
import com.gachigage.global.login.CustomOAuth2User;
import com.gachigage.member.RoleType;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class OAuth2SuccessHandlerTest {
	private static final String frontEndUrl = "http://localhost:3000";
	@Mock
	private JwtProvider jwtProvider;
	@InjectMocks
	private OAuth2SuccessHandler successHandler;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(successHandler, "frontEndUrl", frontEndUrl);
	}

	@Test
	@DisplayName("로그인 성공 시 AccessToken을 쿼리 파라미터에 담아 리다이렉트")
	void redirectWithTokenTest() throws IOException, ServletException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		Authentication authentication = mock(Authentication.class);
		CustomOAuth2User oAuth2User = mock(CustomOAuth2User.class);
		Member member = mock(Member.class);

		// given
		given(authentication.getPrincipal()).willReturn(oAuth2User);
		given(oAuth2User.getAttributes()).willReturn(
			Map.of("kakao_account", Map.of("email", "test@kakao.com"), "id", 1L));
		given(oAuth2User.getMember()).willReturn(member);
		given(member.getRoleType()).willReturn(RoleType.USER);
		given(jwtProvider.generateAccessToken(eq(1L), anyMap())).willReturn("fake-access-token");
		given(response.encodeRedirectURL(anyString())).willAnswer(invocation -> invocation.getArgument(0));

		// when
		successHandler.onAuthenticationSuccess(request, response, authentication);

		// then
		verify(response).sendRedirect(frontEndUrl + "/auth/kakao/callback?token=fake-access-token");
	}
}
