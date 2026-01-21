package com.gachigage.global;

import java.time.LocalDate;
import java.util.HashMap;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import com.gachigage.global.login.CustomOAuth2User;
import com.gachigage.member.Member;
import com.gachigage.member.RoleType;

public class MockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

	@Override
	public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
		SecurityContext context = SecurityContextHolder.createEmptyContext();

		Member member = Member.builder()
			.email(annotation.email())
			.name(annotation.name())
			.roleType(RoleType.valueOf(annotation.role()))
			.birthDate(LocalDate.now())
			.build();

		// Mock CustomOAuth2User 생성
		CustomOAuth2User principal = new CustomOAuth2User(member, new HashMap<>());

		// 인증 토큰 생성
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal,
			"fake-my-accessToken", principal.getAuthorities());

		context.setAuthentication(authentication);

		return context;
	}
}
