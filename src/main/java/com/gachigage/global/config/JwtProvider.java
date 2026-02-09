package com.gachigage.global.config;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtProvider {

	private static final long JWT_TOKEN_VALID = (long)1000 * 60 * 60 * 24; // jwt 만료기간: 24시간

	@Value("${jwt.secret}")
	private String secret;

	private SecretKey key;

	@PostConstruct
	public void init() {
		key = Keys.hmacShaKeyFor(secret.getBytes());
	}

	public String generateAccessToken(final Long id, final Map<String, Object> claims) {
		long currentTimeMills = System.currentTimeMillis();

		return Jwts.builder()
			.id(UUID.randomUUID().toString())
			.claims(claims)
			.subject(String.valueOf(id))
			.issuedAt(new Date(currentTimeMills))
			.expiration(new Date(currentTimeMills + JWT_TOKEN_VALID))
			.signWith(key)
			.compact();
	}

	public String generateAccessToken(final Long id) {
		return generateAccessToken(id, new HashMap<>());
	}

	public Boolean validateToken(final String token) {
		try {
			Jwts.parser()
				.verifyWith(key)
				.build()
				.parseSignedClaims(token);
			return true;
		} catch (SecurityException e) {
			log.warn("Invalid JWT signature: {}", e.getMessage());
		} catch (MalformedJwtException e) {
			log.warn("Invalid JWT token: {}", e.getMessage());
		} catch (ExpiredJwtException e) {
			log.warn("JWT token is expired: {}", e.getMessage());
		} catch (UnsupportedJwtException e) {
			log.warn("JWT token is unsupported: {}", e.getMessage());
		} catch (IllegalArgumentException e) {
			log.warn("JWT claims string is empty: {}", e.getMessage());
		} catch (Exception e) {
			log.warn("JWT validate error! {}", e.getMessage());
		}

		return false;
	}

	public <T> T getClaimFromToken(final String token, final Function<Claims, T> claimsResolver) {
		// token 유효성 검증
		if (!validateToken(token)) {
			return null;
		}

		final Claims claims = getAllClaimsFromToken(token);

		return claimsResolver.apply(claims);
	}

	private Claims getAllClaimsFromToken(final String token) {
		return Jwts.parser()
			.verifyWith(key)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}

	public Authentication getAuthentication(final String token) {
		Claims claims = getAllClaimsFromToken(token);

		// 권한 정보 가져오기
		Collection<? extends GrantedAuthority> authorities =
			Arrays.stream(claims.get("roleType").toString().split(","))
				.map(SimpleGrantedAuthority::new)
				.collect(Collectors.toList());

		// UserDetails를 구현한 User 객체 생성
		User principal = new User(claims.getSubject(), "", authorities);

		// Authentication 객체 리턴
		return new UsernamePasswordAuthenticationToken(principal, token, authorities);
	}

	public String getTokenIdFromToken(final String token) {
		return getClaimFromToken(token, Claims::getId);
	}

	public Long getIdFromToken(final String token) {
		return Long.parseLong(getClaimFromToken(token, Claims::getSubject));
	}

	public String getEmailFromToken(final String token) {
		return getAllClaimsFromToken(token).get("email", String.class);
	}

	public Date getExpirationDateFromToken(final String token) {
		return getClaimFromToken(token, Claims::getExpiration);
	}
}
