package com.gachigage.global;

import static org.assertj.core.api.AssertionsForClassTypes.*;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import com.gachigage.global.config.JwtProvider;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class JwtProviderTest {
	private static final String TEST_SECRET = "testSecretKeytestSecretKeytestSecretKeytestSecretKey";
	private JwtProvider jwtProvider;

	@BeforeEach
	void setUp() {
		jwtProvider = new JwtProvider();

		ReflectionTestUtils.setField(jwtProvider, "secret", TEST_SECRET);

		// @PostConstruct가 자동으로 호출되지 않으므로 수동으로 초기화 메서드를 호출
		jwtProvider.init();
	}

	@Test
	@DisplayName("토큰이 정상적으로 생성")
	void generateAccessTokenTest() {
		// given
		Long id = 1L;

		// when
		String token = jwtProvider.generateAccessToken(id);

		// then
		assertThat(token).isNotNull();
		assertThat(token.length()).isGreaterThan(0);
		System.out.println("Generated Token: " + token);
	}

	@Test
	@DisplayName("유효한 토큰은 True를 반환")
	void validateTokenSuccessTest() {
		// given
		String token = jwtProvider.generateAccessToken(1L);

		// when
		Boolean isValid = jwtProvider.validateToken(token);

		// then
		assertThat(isValid).isTrue();
	}

	@Test
	@DisplayName("생성된 토큰에서 회원정보(email)를 추출")
	void getEmailFromTokenTest() {
		// given
		Long id = 1L;
		HashMap<String, Object> map = new HashMap<>();
		map.put("email", "test@gmail.com");

		String token = jwtProvider.generateAccessToken(id, map);

		// when
		Long extractedId = jwtProvider.getIdFromToken(token);
		String extractedEmail = jwtProvider.getEmailFromToken(token);

		// then
		assertThat(extractedId).isEqualTo(id);
		assertThat(extractedEmail).isEqualTo("test@gmail.com");
	}

	@Test
	@DisplayName("생성된 토큰에서 회원정보(authentication)를 추출")
	void getAuthenticationTest() {
		// given
		Long id = 1L;
		String email = "admin@example.com";
		Map<String, Object> claims = new HashMap<>();
		claims.put("email", email);
		claims.put("roleType", "ROLE_ADMIN");

		String token = jwtProvider.generateAccessToken(id, claims);

		// when
		Authentication authentication = jwtProvider.getAuthentication(token);

		// then
		assertThat(authentication).isNotNull();
		assertThat(authentication.getName()).isEqualTo(String.valueOf(id));
		assertThat(
			authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))).isTrue();
	}

	@Test
	@DisplayName("만료된 토큰은 false를 반환")
	void validateTokenExpiredTest() {
		// given
		SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
		Date past = new Date(System.currentTimeMillis() - 1000); // 1초 전

		String expiredToken = Jwts.builder()
			.id(UUID.randomUUID().toString())
			.subject("user@example.com")
			.expiration(past) // 만료 시간을 과거로 설정
			.signWith(key)
			.compact();

		// when
		Boolean isValid = jwtProvider.validateToken(expiredToken);

		// then
		assertThat(isValid).isFalse();
	}

	@Test
	@DisplayName("잘못된 시그니처(위조된 토큰)는 false를 반환")
	void validateTokenInvalidSignatureTest() {
		// given
		// 다른 비밀키로 서명된 토큰 생성
		String fakeSecret = "fakeSecretKeyfakeSecretKeyfakeSecretKeyfakeSecretKey";
		SecretKey fakeKey = Keys.hmacShaKeyFor(fakeSecret.getBytes(StandardCharsets.UTF_8));

		String forgedToken = Jwts.builder()
			.id(UUID.randomUUID().toString())
			.subject("user@example.com")
			.signWith(fakeKey) // 가짜 키로 서명
			.compact();

		// when
		Boolean isValid = jwtProvider.validateToken(forgedToken);

		// then
		assertThat(isValid).isFalse();
	}

	@Test
	@DisplayName("형식이 잘못된 토큰(Malformed)은 false를 반환")
	void validateTokenMalformedTest() {
		// given
		String garbageToken = "this.is.not.a.valid.token";

		// when
		Boolean isValid = jwtProvider.validateToken(garbageToken);

		// then
		assertThat(isValid).isFalse();
	}
}
