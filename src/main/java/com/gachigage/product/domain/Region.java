package com.gachigage.product.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "Region")
public class Region {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "province", length = 50, nullable = false)
	private String province;

	@Column(name = "city", length = 50, nullable = false)
	private String city;

	@Column(name = "district", length = 50, nullable = false)
	private String district;

	@Column(name = "dong", length = 50, nullable = false)
	private String dong;

	@Builder
	public Region(String province, String city, String district, String dong) {
		this.province = province;
		this.city = city;
		this.district = district;
		this.dong = dong;
	}
}
