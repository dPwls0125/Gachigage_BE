package com.gachigage.member;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

	@Autowired
	MemberRepository memberRepository;

	@Test
	@DisplayName("Email로 멤버 찾기 성공 테스트")
	void findMemberByEmail_Success() {
		String email = "test@gachigage.com";
		Member member = Member.builder()
			.name("Test User")
			.birthDate(LocalDate.of(2001, 1, 1))
			.gender((byte)1)
			.nickname("TestNickname")
			.email(email)
			.roleType(RoleType.USER)
			.build();

		memberRepository.save(member);

		Optional<Member> foundMember = memberRepository.findMemberByEmail(email);

		assertThat(foundMember).isPresent();
		assertThat(foundMember.get().getName()).isEqualTo("Test User");
	}

	@Test
	@DisplayName("Email로 멤버 찾기 실패 테스트")
	void findMemberByEmail_Fail() {
		String email = "test@gachigage.com";
		Member member = Member.builder()
			.name("Test User")
			.birthDate(LocalDate.of(2001, 1, 1))
			.gender((byte)1)
			.nickname("TestNickname")
			.email(email)
			.roleType(RoleType.USER)
			.build();

		memberRepository.save(member);

		Optional<Member> foundMember = memberRepository.findMemberByEmail(email + ".kr");

		assertThat(foundMember).isNotPresent();
	}
}
