package com.gachigage.global.login.service;

import static org.mockito.BDDMockito.*;

import java.util.Optional;

import com.gachigage.member.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.gachigage.member.Member;
import com.gachigage.member.MemberRepository;
import com.gachigage.member.RoleType;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {
	@Mock
	private MemberRepository memberRepository;

	@InjectMocks
	private CustomOAuth2UserService customOAuth2UserService;

	@Test
	@DisplayName("기존 회원이 없으면 새로 저장")
	void saveNewMemberTest() {
		// given
		String email = "test@gmail.com";
		String nickname = "test user";
		String name = "홍길동";
		String birthday = "0101";
		String birthyear = "2000";
		Long oauthId = 1234567891234L;
		String oauthProvider = "kakao";
		RoleType role = RoleType.USER;

		given(memberRepository.findMemberByEmail(email)).willReturn(Optional.empty());
		given(memberRepository.save(any(Member.class))).willAnswer(invocation -> invocation.getArgument(0));

		// when
		Member result = ReflectionTestUtils.invokeMethod(customOAuth2UserService, "saveOrUpdate", oauthId,
			oauthProvider, email, nickname, name, birthday, birthyear, role);

		// then
		verify(memberRepository).save(any(Member.class));
		assert result.getEmail().equals(email);
		assert result.getBirthDate().toString().equals("2000-01-01");
		assert result.getOauthId().equals(oauthId);
		assert result.getOauthProvider().equals(oauthProvider);
	}
}
