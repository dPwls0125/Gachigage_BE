package com.gachigage.product.domain;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.ColumnDefault;

import com.gachigage.global.common.BaseEntity;
import com.gachigage.member.Member;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "Product")
public class Product extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "seller_id", nullable = false)
	private Member seller;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = false)
	private ProductCategory category;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "region_id", nullable = false)
	private Region region;

	@Column(name = "name", length = 50, nullable = false)
	private String name;

	@Column(name = "title", length = 100, nullable = false)
	private String title;

	@Column(name = "description", columnDefinition = "TEXT", nullable = false)
	private String description;

	@Column(name = "stock", nullable = false)
	private Long stock;

	@Column(name = "trade_type", length = 20, nullable = false)
	private String tradeType;

	@ColumnDefault("0")
	@Column(name = "visit_count", nullable = false)
	private int visitCount;

	@ColumnDefault("0")
	@Column(name = "like_count", nullable = false)
	private int likeCount;

	@Column(name = "latitude")
	private Double latitude;

	@Column(name = "longtitude")
	private Double longtitude;

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProductImage> images = new ArrayList<>();

	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProductPrice> prices = new ArrayList<>();

	@Builder
	public Product(Member seller, ProductCategory category, Region region, String name, String title,
		String description, Long stock, String tradeType, Double latitude, Double longtitude) {
		this.seller = seller;
		this.category = category;
		this.region = region;
		this.name = name;
		this.title = title;
		this.description = description;
		this.stock = stock;
		this.tradeType = tradeType;
		this.latitude = latitude;
		this.longtitude = longtitude;
	}
}
