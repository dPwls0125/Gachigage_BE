package com.gachigage.global.login;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.gachigage.member.Member;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.gachigage.member.Member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {

	private final Member member;
	private final Map<String, Object> attributes;

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return Collections.singleton(new SimpleGrantedAuthority(member.getRoleType().name()));
	}

	@Override
	public String getName() {
		return member.getName();
	}
}
