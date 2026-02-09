package com.gachigage.global;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class MockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

	@Override
	public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
		SecurityContext context = SecurityContextHolder.createEmptyContext();

		Collection<? extends GrantedAuthority> authorities = Arrays.stream(annotation.role().split(","))
			.map(SimpleGrantedAuthority::new)
			.toList();

		User user = new User(String.valueOf(annotation.oauthId()), "", authorities);

		// 인증 토큰 생성
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user,
			"fake-my-accessToken", authorities);

		context.setAuthentication(authentication);

		return context;
	}
}
