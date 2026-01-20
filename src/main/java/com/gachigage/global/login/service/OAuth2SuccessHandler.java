package com.gachigage.global.login.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.gachigage.global.config.JwtProvider;
import com.gachigage.global.login.CustomOAuth2User;
import com.gachigage.member.RoleType;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final JwtProvider jwtProvider;

	@Value("${front.url}")
	private String frontEndUrl;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
		Authentication authentication) throws IOException, ServletException {
		CustomOAuth2User oAuth2User = (CustomOAuth2User)authentication.getPrincipal();
		Map<String, Object> attributes = oAuth2User.getAttributes();
		Map<String, Object> kakaoAccount = (Map<String, Object>)attributes.get("kakao_account");
		Long oauthId = (Long)attributes.get("id");
		String email = (String)kakaoAccount.get("email");
		RoleType roleType = oAuth2User.getMember().getRoleType();
		Map<String, Object> claims = new HashMap<>();
		claims.put("email", email);
		claims.put("roleType", roleType);

		String accessToken = jwtProvider.generateAccessToken(oauthId, claims);
		log.warn("accessToken: {}", accessToken);
		String targetUrl = UriComponentsBuilder.fromUriString(frontEndUrl + "/auth/kakao/callback")
			.queryParam("token", accessToken)
			.build()
			.toUriString();

		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}
}
