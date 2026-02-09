package com.gachigage.member;

import java.time.LocalDate;

import com.gachigage.global.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "member")
public class Member extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 50)
	private String name;

	@Column(nullable = false, length = 100, unique = true)
	private String email;

	@Column(length = 50)
	private String nickname;

	@Column(name = "birth_date")
	@Temporal(TemporalType.DATE)
	private LocalDate birthDate;

	private byte gender;

	@Column(name = "image_url")
	private String imageUrl;

	@Column(name = "oauth_id", nullable = false)
	private Long oauthId;

	@Column(name = "oauth_provider")
	private String oauthProvider;

	@Enumerated(EnumType.STRING)
	@Column(name = "role_type")
	private RoleType roleType;

	public void updateNickname(String nickname) {
		this.nickname = nickname;
	}

	public void updateProfileImage(String imageUrl) {
		this.imageUrl = imageUrl;
	}
}
