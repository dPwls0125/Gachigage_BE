package com.gachigage.member;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
	Optional<Member> findMemberByEmail(String email);

	Optional<Member> findMemberByOauthId(Long oauthId);
}
