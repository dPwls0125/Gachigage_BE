package com.gachigage.global.login.service;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.gachigage.global.error.CustomException;
import com.gachigage.global.error.ErrorCode;
import com.gachigage.global.login.CustomOAuth2User;
import com.gachigage.member.Member;
import com.gachigage.member.MemberRepository;
import com.gachigage.member.RoleType;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
	private final MemberRepository memberRepository;

	@Transactional
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

		OAuth2User oAuth2User = super.loadUser(userRequest);
		log.info("oauth2User.getAttributes: {}", oAuth2User.getAttributes());

		Map<String, Object> attributes = oAuth2User.getAttributes();
		Map<String, Object> kakaoAccount = (Map<String, Object>)attributes.get("kakao_account");

		if (kakaoAccount == null) {
			throw new OAuth2AuthenticationException("kakao Info is Missing!");
		}
		Map<String, Object> profile = (Map<String, Object>)kakaoAccount.get("profile");

		String providerName = userRequest.getClientRegistration().getRegistrationId();
		String email = (String)kakaoAccount.get("email");

		if (email == null) {
			throw new CustomException(ErrorCode.USER_NOT_FOUND);
		}

		Long oauthId = (Long)attributes.get("id");
		String birthday = (String)kakaoAccount.get("birthday");
		String birthyear = (String)kakaoAccount.get("birthyear");
		String name = (String)kakaoAccount.get("name");
		String nickname = (String)profile.get("nickname");
		RoleType roleType = RoleType.USER;

		Member member = saveOrUpdate(oauthId, providerName, email, nickname, name, birthday, birthyear, roleType);
		CustomOAuth2User customOAuth2User = new CustomOAuth2User(member, attributes);
		log.info("oauth2userservice -> customoauth2user.getAttributes: {}", customOAuth2User.getAttributes());

		return customOAuth2User;
	}

	private Member saveOrUpdate(Long oauthId, String providerName, String email, String nickname, String name,
		String birthday, String birthyear, RoleType roleType) {
		LocalDate birthDate = LocalDate.of(Integer.parseInt(birthyear), Integer.parseInt(birthday.substring(0, 2)),
			Integer.parseInt(birthday.substring(2)));
		Member member = memberRepository.findMemberByEmail(email)
			.orElse(Member.builder()
				.name(name)
				.email(email)
				.nickname(nickname)
				.birthDate(birthDate)
				.oauthId(oauthId)
				.oauthProvider(providerName)
				.roleType(roleType)
				.build());

		return memberRepository.save(member);
	}
}
