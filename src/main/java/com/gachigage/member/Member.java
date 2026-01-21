package com.gachigage.member;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Member {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 50)
	private String name;

	@Column(nullable = false, length = 100, unique = true)
	private String email;

	@Column(length = 50, unique = true)
	private String nickname;

	@Column(name = "birth_date")
	@Temporal(TemporalType.DATE)
	private LocalDate birthDate;

	private byte gender;

	@Column(name = "image_url")
	private String imageUrl;

	@Column(name = "oauth_id")
	private Long oauthId;

	@Column(name = "oauth_provider")
	private String oauthProvider;

	@Enumerated(EnumType.STRING)
	@Column(name = "role_type")
	private RoleType roleType;
}
