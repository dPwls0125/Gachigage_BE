package com.gachigage.product.dto;

import java.time.LocalDateTime;

import com.gachigage.product.domain.TradeType;
import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProductListResponseDto {

	private Long productId;
	private String title;
	private String mainImageUrl;
	private String province;
	private String city;
	private String group;
	private TradeType tradeType;
	private int price;
	private int quantity;
	private int viewCount;
	private boolean isLiked;
	private LocalDateTime createdAt;

	@QueryProjection
	public ProductListResponseDto(Long productId, String title, String mainImageUrl, String province, String city,
		String group,
		TradeType tradeType, int price, int quantity, int viewCount, boolean isLiked, LocalDateTime createdAt) {
		this.productId = productId;
		this.title = title;
		this.mainImageUrl = mainImageUrl;
		this.province = province;
		this.city = city;
		this.group = group;
		this.tradeType = tradeType;
		this.price = price;
		this.quantity = quantity;
		this.viewCount = viewCount;
		this.isLiked = isLiked;
		this.createdAt = createdAt;
	}
}
